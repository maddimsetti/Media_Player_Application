package com.example.mediaplayerapp.listeners

interface AuthenticationListener {

    fun onRegister(status: Boolean, exception: String?)
    fun onLogin(status: Boolean, exception: String?)
    fun onResetPassword(status: Boolean)
}