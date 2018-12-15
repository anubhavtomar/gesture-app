package com.example.anubhav.gesture_app

import android.util.Log
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.InputStream

class networkClient {

    private val client = OkHttpClient()

    fun postApi (sampleName : String , FILE_RECORDING : File) {
        Log.d("testing", "----------------caughtDebugger2------------")
        val MEDIA_TYPE_AAC = MediaType.parse("audio/*")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", "${sampleName}.aac", RequestBody.create(MEDIA_TYPE_AAC, FILE_RECORDING))
            .build()

        Log.d("testingRequestBody", requestBody.toString())
        Log.d("testing", "----------------caughtDebugger3------------")
        val request = Request.Builder()
            .url("http://172.24.21.173:5000/audio-uploader")
            .post(requestBody)
            .build()
//                    val response = client.newCall(request).execute()
        Log.d("testing", "----------------caughtDebugger4------------")
        client.newCall(request).execute()
        Log.d("testing", "----------------caughtDebugger5------------")
//        client.newCall(request).enqueue(object : Callback<> {
//            override fun onResponse(call: Call<Cook>?, response: Response<Cook>) = success(response)
//
//            override fun onFailure(call: Call<Cook>?, t: Throwable) = failure(t)
//        })
    }
}