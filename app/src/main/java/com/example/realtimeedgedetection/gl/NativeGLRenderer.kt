package com.example.realtimeedgedetection.gl

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.opengl.GLSurfaceView
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NativeGLRenderer : GLSurfaceView.Renderer {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    // Native methods
    external fun initRenderer()
    external fun drawFrame()
    external fun updateFrame(data: ByteArray, width: Int, height: Int)

    // 🔹 Store last processed frame
    private var lastFrame: ByteArray? = null
    private var frameWidth: Int = 0
    private var frameHeight: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        initRenderer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // viewport handled in native
    }

    override fun onDrawFrame(gl: GL10?) {
        drawFrame()
    }

    // 🔹 Called from MainActivity after processFrame
    fun updateFrameBuffer(data: ByteArray, width: Int, height: Int) {
        lastFrame = data
        frameWidth = width
        frameHeight = height
        updateFrame(data, width, height)
    }

    // 🔹 Save latest processed frame to PNG
    fun saveCurrentFrame(context: Context) {
        if (lastFrame == null || frameWidth == 0 || frameHeight == 0) {
            Log.e("NativeGLRenderer", "⚠ No frame available to save")
            return
        }

        val bmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ALPHA_8)
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(lastFrame!!))

        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EdgeDetection")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "processed.png")
        FileOutputStream(file).use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        Log.i("NativeGLRenderer", "✅ Frame saved at ${file.absolutePath}")
    }
}
