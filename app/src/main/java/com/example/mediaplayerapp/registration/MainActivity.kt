package com.example.mediaplayerapp.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.dashboard.DashBoardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAuthentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuthentication = FirebaseAuth.getInstance()    //Initialize Firebase Authentication

        registration_signIn.setOnClickListener(this)
        registration_register.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuthentication.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser != null){
            startActivity(Intent(this@MainActivity, DashBoardActivity::class.java))
            finish()
        } else {
            Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.registration_signIn -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.registration_fragment_container, LoginFragment())
                    .setReorderingAllowed(true).addToBackStack(null).commit()
            }
            R.id.registration_register -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.registration_fragment_container, RegistrationFragment())
                    .setReorderingAllowed(true).addToBackStack(null).commit()
            }
        }
    }
}