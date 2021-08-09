package com.example.mediaplayerapp.dashboard

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.listeners.AuthenticationListener
import com.example.mediaplayerapp.listeners.DashBoardListener
import com.example.mediaplayerapp.listeners.PermissionsListener
import com.example.mediaplayerapp.permission.Permissions
import com.example.mediaplayerapp.pojoclass.ProfileDetails
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import kotlinx.android.synthetic.main.dialog_forgot_password.view.*
import kotlinx.android.synthetic.main.dialog_profile_details.*
import kotlinx.android.synthetic.main.dialog_profile_details.view.*
import kotlinx.android.synthetic.main.toolbar_profile.*
import kotlinx.android.synthetic.main.toolbar_profile.view.*
import java.io.ByteArrayOutputStream


class DashBoardActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 2001
    private lateinit var permissionForCaptureImage: Permissions
//    lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        bottomNavigationView.background = null
        setSupportActionBar(custom_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        custom_toolbar.showOverflowMenu()

        permissionForCaptureImage = Permissions() //permissions class
//        profileImage = findViewById(R.id.profile_details_image)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_action_items, menu)

        val menuItem = menu?.findItem(R.id.toolbar_profileImage)
        val view: View = MenuItemCompat.getActionView(menuItem)

        val profile = view.toolbar_profile_image

        profile.setOnClickListener {
            viewUserProfileDetails()
            Toast.makeText(this@DashBoardActivity, "Profile Image Selected", Toast.LENGTH_SHORT)
                .show()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.toolbar_search -> {
                Toast.makeText(this@DashBoardActivity, "search", Toast.LENGTH_SHORT).show()
            }

            R.id.toolbar_upload_videos -> {
                selectingImage()
                Toast.makeText(this@DashBoardActivity, "Uploading New Content", Toast.LENGTH_SHORT)
                    .show()

            }
        }
        return true
    }

    private fun viewUserProfileDetails() {
        val builder = AlertDialog.Builder(this@DashBoardActivity)
        val view = layoutInflater.inflate(R.layout.dialog_profile_details, null)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val firebaseReference = FirebaseDatabase.getInstance().getReference("User Details")
        val userID = firebaseUser?.uid

        if (userID != null) {
            firebaseReference.child(userID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfileDetails: ProfileDetails? =
                            snapshot.getValue(ProfileDetails::class.java)
                        if (userProfileDetails != null) {
                            val userFullName = userProfileDetails.fullName
                            val userEmailID = userProfileDetails.email
                            val userPhoneNumber = userProfileDetails.phoneNumber

                            view.profile_details_name.text = userFullName
                            view.profile_details_email.text = userEmailID
                            view.profile_details_phone.text = userPhoneNumber
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@DashBoardActivity, "Fetching the Data From Firebase Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        }
        view.profile_details_image.setOnClickListener() {
            selectingImage()
        }
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


    private val listener: DashBoardListener = object : DashBoardListener {
        override fun onRetrieveProfileDetailsFromFirebase(status: Boolean) {
            if (status) {
                Log.d(TAG, "onRetrieveProfileDetailsFromFirebase: Success $status")

            } else {
                Toast.makeText(this@DashBoardActivity, "Fetching the Data From Firebase Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun selectingImage() {
        val pictureDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItem =
            arrayOf("Select photo from Gallery", "Capture Photo from Camera")
        pictureDialog.setItems(pictureDialogItem) { dialog, selection ->
            when (selection) {
                0 -> permissionForCaptureImage.galleryCheckPermission(this@DashBoardActivity, permissionListeners)
                1 -> permissionForCaptureImage.cameraCheckPermission(this@DashBoardActivity, permissionListeners)
            }
        }
        pictureDialog.show()
    }

    private val permissionListeners: PermissionsListener = object : PermissionsListener {

        override fun checkPermissionForGallery(status: Boolean, context: Context) {
            if(status) {
                selectImageFromGallery()
                Log.d(TAG, "checkPermissionForGallery: Success $status")
            } else {
                Log.d(TAG, "checkPermissionForGallery: Failure $status")
                Toast.makeText(
                    applicationContext, "You have denied the storage to select Image",
                    Toast.LENGTH_SHORT
                ).show()

                showRotationalDialogForPermission(context)
            }
        }

        override fun checkPermissionForCamera(status: Boolean, context: Context) {
            if(status) {
                selectImageFromCamera()
                Log.d(TAG, "checkPermissionForCamera: Success $status")
            } else {
                Log.d(TAG, "checkPermissionForCamera: Failure $status")
                Toast.makeText(
                    applicationContext, "You have denied the storage to select Image",
                    Toast.LENGTH_SHORT
                ).show()

                showRotationalDialogForPermission(context)
            }
        }

    }

    private fun showRotationalDialogForPermission(context: Context) {
        android.app.AlertDialog.Builder(context).setMessage(
            "It looks like you have turned off Permissions"
                    + "required for this feature. It can be enable under App Settings"
        )
            .setPositiveButton("Go To Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", "packageName", null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun selectImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    gettingDataAsBitmap(data)
                }

                GALLERY_REQUEST_CODE -> {
                    gettingDataAsBitmap(data)
                }
            }
        }
    }

    private fun gettingDataAsBitmap(data: Intent?) {
        val bitmap = data?.extras?.get("data") as? Bitmap
        val view = layoutInflater.inflate(R.layout.dialog_profile_details, null)
        view.profile_details_image.setImageBitmap(bitmap)
        if (bitmap != null) {
            handleUpload(bitmap)
        }
    }

    private fun handleUpload(bitmap: Bitmap) {

        val boas = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val reference: StorageReference = FirebaseStorage.getInstance().reference
            .child("ProfileImages")
            .child("$uid.jpeg")

        reference.putBytes(boas.toByteArray()).addOnSuccessListener {
            getDownloadUrl(reference)
        }
            .addOnFailureListener {
                Log.e(TAG, "OnFailure: ", it)
            }
    }


    private fun getDownloadUrl(reference: StorageReference) {
        reference.downloadUrl.addOnSuccessListener { uri ->
            Log.d("DownloadUrl", "OnSuccess$uri")
            setUserProfileUrl(uri)
        }
    }

    private fun setUserProfileUrl(uri: Uri?) {
        val user = FirebaseAuth.getInstance().currentUser

        val request = UserProfileChangeRequest.Builder().setPhotoUri(uri).build()

        user?.updateProfile(request)?.addOnSuccessListener {
            Toast.makeText(
                this@DashBoardActivity,
                "Successfully Uploaded",
                Toast.LENGTH_SHORT
            ).show()
        }
            ?.addOnFailureListener {
                Toast.makeText(
                    this@DashBoardActivity,
                    "Uploading Failed...",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

}