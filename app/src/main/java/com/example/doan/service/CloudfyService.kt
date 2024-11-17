package com.example.doan.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudfyService {
    @Multipart
    @POST("/upload")
    fun uploadFile(
        @Part("key") apiKey: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Call<ResponseBody?>?

}