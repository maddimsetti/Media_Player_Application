package com.example.mediaplayerapp.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.dashboard.DashBoardActivity
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.pojoclass.ProfileDetails
import com.example.mediaplayerapp.service.ValidatingAuthentication
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_registration.*

class RegistrationFragment : Fragment(), View.OnClickListener {

    private lateinit var mAuthentication: FirebaseAuth
    private lateinit var validatingAuthentication: ValidatingAuthentication

    private var isAllFieldsChecked: Boolean = false

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

        when(view?.id) {
            R.id.registerButton_register_page -> {
                isAllFieldsChecked = userRegisterValidation()
                if(isAllFieldsChecked) {
                    userRegistration()
                }
            }

            else -> null
        }

    }

    private fun userRegisterValidation(): Boolean {
        val name = fullName_register_page.text.toString().trim()
        val email = emailID_register_page.text.toString().trim()
        val password = password_register_page.text.toString().trim()
        val phoneNumber = phoneNumber_register_page.text.toString().trim()

        if(name.isEmpty()) {
            fullName_register_page.error = "Please Enter Your Name"
            fullName_register_page.requestFocus()
            return false
        }

        if(email.isEmpty()) {
            emailID_register_page.error = "Please Enter Email Address"
            emailID_register_page.requestFocus()
            return false
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailID_register_page.error = "Please Enter the Valid Email Address"
            emailID_register_page.requestFocus()
            return false
        }

        if(password.isEmpty()) {
            password_register_page.error = "Please Enter Password"
            password_register_page.requestFocus()
            return false
        } else if(password.length < 8) {
            password_register_page.error = "Minimum length should be 8 characters"
            password_register_page.requestFocus()
            return false
        }

        if(phoneNumber.isEmpty()) {
            phoneNumber_register_page.error = "Please Enter Email Address"
            phoneNumber_register_page.requestFocus()
            return false
        }
        return if(phoneNumber.length == 10) {
            true
        } else {
            phoneNumber_register_page.error = "Phone Number should contains 10 digits"
            phoneNumber_register_page.requestFocus()
            false
        }

        return true
    }

    private fun userRegistration() {
        validatingAuthentication.creatingUserRegistrationAccount(emailID_register_page.text.toString(),
                password_register_page.text.toString(), listener)

    }

    private  val listener: AuthenticationListener = object: AuthenticationListener {
        override fun onRegister(status: Boolean, exception: String?) {

            if(status) {
                Log.d(TAG, "CreatingUserWithEmail: Success $status ")
                Toast.makeText(context, "User Registration Succeeded",
                    Toast.LENGTH_SHORT).show()

                val name = fullName_register_page.text.toString()
                val eMail = emailID_register_page.text.toString()
                val phoneNumber = phoneNumber_register_page.text.toString()

                val profileList = ProfileDetails(name, eMail, phoneNumber)
//                val profileDetails = mutableListOf<ProfileDetails>()
//                profileDetails.add(profileList)

                FirebaseAuth.getInstance().currentUser?.uid?.let {
                    FirebaseDatabase.getInstance().getReference("User Details")
                        .child(it).setValue(profileList)
                }

                val intent: Intent = Intent(activity, DashBoardActivity::class.java)
                activity?.startActivity(intent)

                fullName_register_page.text.clear()
                emailID_register_page.text.clear()
                password_register_page.text.clear()
                phoneNumber_register_page.text.clear()
            } else {
                Log.d(TAG, "CreatingUserWithEmail: Failed")
                Toast.makeText(context, exception,
                    Toast.LENGTH_SHORT).show()
            }
        }

        override fun onLogin(status: Boolean, exception: String?) {
            TODO("Not yet implemented")
        }

        override fun onResetPassword(status: Boolean) {
            TODO("Not yet implemented")
        }

    }

    companion object {
        const val  TAG = "Registration"
    }

}