package com.example.mediaplayerapp.service

import android.util.Log
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.network.LoginListener
import com.example.mediaplayerapp.network.LoginLoader
import com.example.mediaplayerapp.network.MediaPlayerLoginRequest
import com.example.mediaplayerapp.network.MediaPlayerLoginResponse
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class ValidatingAuthentication {

    private lateinit var mAuthentication: FirebaseAuth

    fun creatingUserRegistrationAccount(email: String, password: String, listener: AuthenticationListener) {
        mAuthentication = FirebaseAuth.getInstance()

        mAuthentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
            if(task.isSuccessful) {
                listener.onRegister(true, null)

            } else {
                //If Registration Fails, display a message to user
                Log.w(TAG, "creatingUserRegistrationAccount: failure", task.exception)

                if(task.exception is FirebaseAuthUserCollisionException) {
                    listener.onRegister(false, "Your Email ID already Exists")
                } else {
                    task.exception?.message?.let {
                        listener.onRegister(false, it)
                    }
                }
            }
        }
    }

    fun userLoginIntoAccount(email: String, password: String, listener: AuthenticationListener) {
        mAuthentication = FirebaseAuth.getInstance()

        mAuthentication.signInWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                //Sign-In success, Update UI
                Log.d(TAG, "userLoginIntoAccount: Success")
                listener.onLogin(true, null)
            } else {
                //If Sign-In fails, display a message to the User
                Log.w(TAG, "userLoginIntoAccount: failure", task.exception)
                listener.onLogin(false, "User Login Account Failed")
            }
        }
    }

    fun userResetPasswordForSignIn(email: String, listener: AuthenticationListener) {
        mAuthentication = FirebaseAuth.getInstance()

        mAuthentication.sendPasswordResetEmail(email).addOnCompleteListener() { task ->
            if(task.isSuccessful) {
                Log.d(TAG, "userResetPasswordForSignIn: Success")
                listener.onResetPassword(true)
            }
        }
    }

    fun userLoginIntoAccountUsingAPI(email: String, password: String, listener: AuthenticationListener) {
        LoginLoader().loginUserWithEmailIdPassword(MediaPlayerLoginRequest(email, password, true),
            object : LoginListener {
                override fun onLogin(loginResponse: MediaPlayerLoginResponse) {
                    if (loginResponse.registered) {
                        //Sign-In success, Update UI
                        Log.d(TAG, "userLoginIntoAccount: Success")
                        listener.onLogin(true, null)
                    } else {

                    }
                }

                override fun onFailure(errorMessage: String) {
                    //If Sign-In fails, display a message to the User
                    Log.d(TAG, "userLoginIntoAccount: failure $errorMessage")
                    listener.onLogin(false, errorMessage)
                }

            })
    }

}