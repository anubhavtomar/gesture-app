package com.example.anubhav.gesture_app

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.InputStream

class networkClient {

    private val client = OkHttpClient()

    fun postApi (url: String, sampleName : String , audioFile : File): InputStream {
        Log.d("testing", "----------------caughtDebugger3------------")
        val mediaTypeAAC = MediaType.parse("audio/*")
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", "$sampleName", RequestBody.create(mediaTypeAAC, audioFile))
            .build()

        Log.d("testing", "----------------caughtDebugger4------------")
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        Log.d("testing", "----------------caughtDebugger5------------")
        return response.body()!!.byteStream()
    }
}