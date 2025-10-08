package com.example.realtimeedgedetection.gl

import android.opengl.GLSurfaceView
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

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        initRenderer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // viewport is handled in native
    }

    override fun onDrawFrame(gl: GL10?) {
        drawFrame()
    }
}
