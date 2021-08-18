package com.example.mediaplayerapp.network

data class MediaPlayerLoginRequest(val email: String, val password: String, val returnSecureToken: Boolean)
