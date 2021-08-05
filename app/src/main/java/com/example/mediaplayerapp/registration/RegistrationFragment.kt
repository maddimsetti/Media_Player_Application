package com.example.mediaplayerapp.registration

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.service.ValidatingAuthentication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_registration.*

class RegistrationFragment : Fragment(), View.OnClickListener {

    private lateinit var mAuthentication: FirebaseAuth

    private lateinit var validatingAuthentication: ValidatingAuthentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuthentication = FirebaseAuth.getInstance()    //Initialize Firebase Authentication
        validatingAuthentication = ValidatingAuthentication()

        registerButton_register_page.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        userRegistration()
    }

    private fun userRegistration() {
        val name = fullName_register_page.text.toString().trim()
        val email = emailID_register_page.text.toString().trim()
        val password = password_register_page.text.toString().trim()
        val phoneNumber = phoneNumber_register_page.text.toString().trim()


        if(name.isEmpty() && email.isEmpty()
            && password.isEmpty() && phoneNumber.isEmpty()) {

            Toast.makeText(context, "Please Enter the Required Fields", Toast.LENGTH_SHORT).show()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailID_register_page.error = "Please Enter the Valid Email Address"
            emailID_register_page.requestFocus()
            return
        }

        if(password.length < 8) {
            password_register_page.error = "Minimum length should be 8 characters"
            password_register_page.requestFocus()
            return
        }

        validatingAuthentication.creatingUserRegistrationAccount(email, password, listener)
    }

    private  val listener: AuthenticationListener = object: AuthenticationListener {
        override fun onRegister(status: Boolean, exception: String?) {

            if(status) {
                Log.d(TAG, "CreatingUserWithEmail: Success $status ")
                Toast.makeText(context, "User Registration Succeeded",
                    Toast.LENGTH_SHORT).show()
                fullName_register_page.text.clear()
                emailID_register_page.text.clear()
                password_register_page.text.clear()
                phoneNumber_register_page.text.clear()
            } else {
                Log.d(TAG, "CreatingUserWithEmail: Failed")
                Toast.makeText(context, "User Registration Failed",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    companion object {
        const val  TAG = "Registration"
    }

}