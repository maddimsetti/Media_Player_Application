package com.example.mediaplayerapp.service

import android.util.Log
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.pojoclass.ProfileDetails
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_registration.*

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
}