package com.example.realtimeedgedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimeedgedetection.gl.NativeGLRenderer

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    // JNI functions
    external fun testNative()
    external fun processFrame(frameData: ByteArray, width: Int, height: Int): ByteArray

    // UI references
    private lateinit var textureView: TextureView
    private lateinit var debugText: TextView
    private lateinit var glSurfaceView: GLSurfaceView

    // Camera
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader

    private lateinit var renderer: NativeGLRenderer

    // FPS tracking
    private var lastTime = System.currentTimeMillis()
    private var frameCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI
        textureView = findViewById(R.id.textureView)
        debugText = findViewById(R.id.debugText)
        glSurfaceView = findViewById(R.id.glSurfaceView)

        // GLSurfaceView setup
        renderer = NativeGLRenderer()
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Transparent overlay
        glSurfaceView.setZOrderOnTop(true)
        glSurfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

        // Ask for permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        // Test JNI
        testNative()
        Log.i("MainActivity", "✅ testNative() called successfully")

        // TextureView listener
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        }
    }

    // Permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Log.e("MainActivity", "❌ Camera permission denied")
        }
    }

    // Extract tight Y-plane
    private fun extractTightY(image: Image): ByteArray {
        val w = image.width
        val h = image.height
        val yPlane = image.planes[0]
        val yBuf = yPlane.buffer
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride

        val out = ByteArray(w * h)
        var dst = 0
        val pos = yBuf.position()

        for (row in 0 until h) {
            val rowStart = pos + row * rowStride
            yBuf.position(rowStart)
            if (pixelStride == 1) {
                yBuf.get(out, dst, w)
                dst += w
            } else {
                var col = 0
                while (col < w) {
                    out[dst++] = yBuf.get()
                    yBuf.position(yBuf.position() + pixelStride - 1)
                    col++
                }
            }
        }
        return out
    }

    private fun openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0]

        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

        imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val data = extractTightY(image)
            image.close()

            val processed = processFrame(data, previewSize.width, previewSize.height)
            renderer.updateFrame(processed, previewSize.width, previewSize.height)

            glSurfaceView.requestRender()

            // FPS counter
            frameCount++
            val now = System.currentTimeMillis()
            if (now - lastTime >= 1000) {
                val fps = frameCount
                frameCount = 0
                lastTime = now
                runOnUiThread {
                    debugText.text = "Frame: ${previewSize.width}x${previewSize.height} | FPS: $fps"
                }
            }
        }, null)

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                val surfaceTexture = textureView.surfaceTexture!!
                surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
                val previewSurface = Surface(surfaceTexture)

                camera.createCaptureSession(listOf(previewSurface, imageReader.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession = session
                            val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                            requestBuilder.addTarget(previewSurface)
                            requestBuilder.addTarget(imageReader.surface)
                            session.setRepeatingRequest(requestBuilder.build(), null, null)
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("Camera", "❌ Capture session configuration failed")
                        }
                    }, null)
            }

            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {
                Log.e("Camera", "❌ Camera error: $error")
            }
        }, null)
    }
}
