package com.example.mediaplayerapp.network

data class MediaPlayerLoginResponse(val idToken: String, val email: String, val refreshToken: String,
                                    val expiresIn: String, val localId: String, val registered: Boolean)