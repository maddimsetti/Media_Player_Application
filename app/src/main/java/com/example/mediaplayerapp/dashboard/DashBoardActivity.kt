package com.example.mediaplayerapp.dashboard

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.MenuItemCompat
import com.example.mediaplayerapp.R
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import kotlinx.android.synthetic.main.dialog_forgot_password.view.*
import kotlinx.android.synthetic.main.dialog_profile_details.*
import kotlinx.android.synthetic.main.dialog_profile_details.view.*
import kotlinx.android.synthetic.main.toolbar_profile.view.*

class DashBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        bottomNavigationView.background = null
        setSupportActionBar(custom_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        custom_toolbar.showOverflowMenu()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_action_items, menu)

        val menuItem = menu?.findItem(R.id.toolbar_profileImage)
        val view: View = MenuItemCompat.getActionView(menuItem)

        val profile = view.toolbar_profile_image

        profile.setOnClickListener {
            viewUserProfileDetails()
            Toast.makeText(this@DashBoardActivity, "Profile Image Selected", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.toolbar_search -> {
                Toast.makeText(this@DashBoardActivity, "search", Toast.LENGTH_SHORT).show()
            }

            R.id.toolbar_upload_videos -> {
                Toast.makeText(this@DashBoardActivity, "Uploading New Content", Toast.LENGTH_SHORT).show()

            }
        }
        return true
    }

    private fun viewUserProfileDetails() {
        val builder = AlertDialog.Builder(this@DashBoardActivity)
        val view = layoutInflater.inflate(R.layout.dialog_profile_details, null)
        val intent: Intent = intent
        val userName = intent.getStringExtra("NAME")
        val userEmail = intent.getStringExtra("EMAIL")
        val userPhoneNumber = intent.getStringExtra("PHONENUMBER")

        view.profile_details_name.text = userName
        view.profile_details_email.text = userEmail
        view.profile_details_phone.text = userPhoneNumber

        builder.setView(view)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        view.profile_details_cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        view.profile_details_updateBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }



}