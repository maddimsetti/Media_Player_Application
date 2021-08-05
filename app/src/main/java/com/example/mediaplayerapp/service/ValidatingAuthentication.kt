package com.example.mediaplayerapp.service

import android.util.Log
import com.example.mediaplayerapp.listeners.AuthenticationListener
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
}