package com.example.mediaplayerapp.registration

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.dashboard.DashBoardActivity
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.example.mediaplayerapp.service.ValidatingAuthentication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.dialog_forgot_password.*
import kotlinx.android.synthetic.main.dialog_forgot_password.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*

class LoginFragment : Fragment(), View.OnClickListener {

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
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuthentication = FirebaseAuth.getInstance()    //Initialize Firebase Authentication
        validatingAuthentication = ValidatingAuthentication()

        loginButton_login_page.setOnClickListener(this)
        forgotPassword_login_page.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.loginButton_login_page -> {
                userLoginUsingFirebase()
                val intent: Intent = Intent(activity, DashBoardActivity::class.java)
                activity?.startActivity(intent)
            }

            R.id.forgotPassword_login_page -> {
                onClickForgotPasswordButton()
            }
        }
    }

    private fun userLoginUsingFirebase() {
        val email = email_login_page.text.toString().trim()
        val password = password_login_page.text.toString().trim()

        if(email.isEmpty()
            && password.isEmpty()) {

            Toast.makeText(context, "Please Enter the Required Fields", Toast.LENGTH_SHORT).show()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_login_page.error = "Please Enter the Valid Email Address"
            email_login_page.requestFocus()
            return
        }

        if(password.length < 8) {
            password_login_page.error = "Minimum length should be 8 characters"
            password_login_page.requestFocus()
            return
        }
        validatingAuthentication.userLoginIntoAccount(email, password, listener)

    }

    private fun onClickForgotPasswordButton() {
        val builder = AlertDialog.Builder(activity)
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val emailAddress = view.forgot_password_text
        builder.setView(view)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        view.forgot_password_cancelBtn.setOnClickListener() {
            alertDialog.dismiss()
        }
        view.forgot_password_resetBtn.setOnClickListener() {
            userResetNewPassword(emailAddress)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun userResetNewPassword(emailAddress: EditText) {
        if(emailAddress.text.toString().isEmpty()) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress.text.toString()).matches()) {
            return
        }

        validatingAuthentication.userResetPasswordForSignIn(emailAddress.text.toString(), listener)
    }

    private val listener: AuthenticationListener = object: AuthenticationListener {
        override fun onRegister(status: Boolean, exception: String?) {
            TODO("Not yet implemented")
        }

        override fun onLogin(status: Boolean, exception: String?) {
            if (status) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "SignInWithEmailAddress: Success $status")
                Toast.makeText(context, "Login Authentication Succeeded", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // If sign in fails, display a message to the user.
                Log.d(TAG, "SignInWithEmailAddress: Failed $status")
                Toast.makeText(context, exception, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResetPassword(status: Boolean) {
            if(status) {
                Log.d(TAG, "onResetPassword: Success $status")
                Toast.makeText(context, "Password Reset Link sent to MailSuccessFull",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

}