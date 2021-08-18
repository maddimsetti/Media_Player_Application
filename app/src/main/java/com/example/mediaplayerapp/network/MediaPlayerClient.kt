package com.example.mediaplayerapp.network

import android.media.MediaPlayer
import com.google.gson.GsonBuilder

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class MediaPlayerClient private constructor() {
    companion object {
        var instance: MediaPlayerClient? = null
        get() {
            if (field == null) {
                field = MediaPlayerClient()
            }
            return field
        } private set
        private lateinit var client: OkHttpClient

    }

    init {
        val httpClient = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        httpClient.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")
            val request: Request = requestBuilder.build()
            return@Interceptor chain.proceed(request)
        }).addInterceptor(loggingInterceptor)
        client = httpClient
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl : String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .client(client).build()

    fun MediaPlayerRestAdapter(baseUrl : String): MediaPlayerAPI = retrofit(baseUrl)
        .create(MediaPlayerAPI::class.java)



}