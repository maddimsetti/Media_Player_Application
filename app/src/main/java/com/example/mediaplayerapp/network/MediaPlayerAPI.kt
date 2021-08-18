package com.example.mediaplayerapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface MediaPlayerAPI {
    @POST("/v1/accounts:signInWithPassword?key=AIzaSyA4aezrDpg0YiMPjPNLxdx377asN8B2GpY")
    fun loginUser(@Body request: MediaPlayerLoginRequest): Call<MediaPlayerLoginResponse>
}