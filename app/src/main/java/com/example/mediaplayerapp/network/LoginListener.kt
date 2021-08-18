package com.example.mediaplayerapp.network

import retrofit2.Response

interface LoginListener {
    fun onLogin(loginResponse: MediaPlayerLoginResponse)
    fun onFailure(errorMessage: String)
}