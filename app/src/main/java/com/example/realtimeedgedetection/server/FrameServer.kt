package com.example.realtimeedgedetection.server

import android.content.Context
import android.os.Environment
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File

class FrameServer(private val context: Context) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession?): Response {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "EdgeDetection/processed.png")

        return if (file.exists()) {
            Log.i("FrameServer", "✅ Serving frame: ${file.absolutePath}")
            newFixedLengthResponse(Response.Status.OK, "image/png", file.inputStream(), file.length())
        } else {
            newFixedLengthResponse("❌ No frame available yet")
        }
    }
}
