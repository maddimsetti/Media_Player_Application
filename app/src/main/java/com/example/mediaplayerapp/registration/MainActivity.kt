package com.example.mediaplayerapp.registration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.mediaplayerapp.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registration_signIn.setOnClickListener(this)
        registration_register.setOnClickListener(this)
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