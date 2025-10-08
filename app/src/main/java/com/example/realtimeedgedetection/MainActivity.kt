package com.example.realtimeedgedetection

import android.Manifest
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimeedgedetection.gl.NativeGLRenderer

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    // Native JNI function for testing
    external fun testNative()

    // Native JNI function for frame processing
    external fun processFrame(frameData: ByteArray, width: Int, height: Int): ByteArray

    private lateinit var textureView: TextureView
    private lateinit var debugText: TextView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: NativeGLRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        renderer = NativeGLRenderer()
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // ✅ Call test function (Stage 3 check)
        testNative()
        Log.i("MainActivity", "✅ testNative() called successfully")

        // Initialize UI elements
        textureView = findViewById(R.id.textureView)
        debugText = findViewById(R.id.debugText)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            @RequiresPermission(Manifest.permission.CAMERA)
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0] // back camera

        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

        imageReader = ImageReader.newInstance(
            previewSize.width, previewSize.height,
            ImageFormat.YUV_420_888, 2
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            // ✅ Send frame to native OpenCV
            val processed = processFrame(data, image.width, image.height)
            renderer.updateFrame(processed, image.width, image.height)
            // Update debug overlay
            runOnUiThread {
                debugText.text = "Frame processed: ${image.width}x${image.height}"
            }

            image.close()
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
                            val requestBuilder =
                                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                            requestBuilder.addTarget(previewSurface)
                            requestBuilder.addTarget(imageReader.surface)
                            session.setRepeatingRequest(requestBuilder.build(), null, null)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("Camera", "Capture session configuration failed")
                        }
                    }, null)
            }

            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {
                Log.e("Camera", "Error: $error")
            }
        }, null)
    }
}
