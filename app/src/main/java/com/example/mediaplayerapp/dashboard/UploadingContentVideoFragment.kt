package com.example.mediaplayerapp.dashboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.provider.UserDictionary
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.listeners.PermissionsListener
import com.example.mediaplayerapp.mediaplayer.MediaPlayer
import com.example.mediaplayerapp.permission.Permissions
import com.example.mediaplayerapp.pojoclass.ProfileDetails
import com.example.mediaplayerapp.registration.RegistrationFragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_uploading_content_video.*
import kotlinx.android.synthetic.main.upload_content_toolbar.*
import kotlinx.android.synthetic.main.upload_content_toolbar.view.*
import java.text.SimpleDateFormat
import java.util.*

class UploadingContentVideoFragment: Fragment(), View.OnClickListener {

    private lateinit var permissionForCaptureImage: Permissions
    private var currentDate: String? = null
    private var videoUri: Uri? = null

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mediaPlayer: MediaPlayer

    private val VIDEO_REQUEST_CODE = 1431
    private val VIDEO_CAMERA_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_uploading_content_video, container, false)
        mediaPlayer = MediaPlayer()
        databaseReference = FirebaseDatabase.getInstance().getReference("MediaPlayer List")
        permissionForCaptureImage = Permissions() //permissions class
        view.content_create_add.setOnClickListener(this)
        view.content_create_save.setOnClickListener(this)
        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateAndTime = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        currentDate = dateAndTime.format(java.util.Date())
        content_upload_dateTime.text = currentDate

        content_upload_backButton.setOnClickListener(View.OnClickListener {
            val intent: Intent = Intent(activity, DashBoardActivity::class.java)
            activity?.startActivity(intent)
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.content_create_add -> {
                content_upload_title.text = null
                content_upload_description.text = null
                content_upload_dateTime.text = currentDate

                selectingVideoToUpload()
                Toast.makeText(activity, "Click on create new video Button", Toast.LENGTH_SHORT).show()
            }
            R.id.content_create_save -> {
                uploadVideoToFirebase()
                Toast.makeText(activity, "Click on Save the video Button", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectingVideoToUpload() {
        val pictureDialog = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        pictureDialog?.setTitle("Select Action")
        val pictureDialogItem =
            arrayOf("Upload Video from Gallery", "Record Video from Camera")
        pictureDialog?.setItems(pictureDialogItem) { _, selection ->
            when (selection) {
                0 -> context?.let {
                    permissionForCaptureImage.galleryCheckPermission(it, permissionForUploadingContentListener)
                }
                1 -> context?.let {
                    permissionForCaptureImage.cameraCheckPermission(it, permissionForUploadingContentListener)
                }
            }
        }
        pictureDialog?.show()
    }

    private val permissionForUploadingContentListener: PermissionsListener =
        object : PermissionsListener {

            override fun checkPermissionForGallery(status: Boolean, context: Context) {
                if (status) {
                    selectVideoContentToUpload()
                    Log.d(RegistrationFragment.TAG, "checkPermissionForGallery: Success $status")
                } else {
                    Log.d(RegistrationFragment.TAG, "checkPermissionForGallery: Failure $status")
                    Toast.makeText(
                        context, "You have denied the storage to select Video",
                        Toast.LENGTH_SHORT).show()

                    showRotationalDialogForPermission(context)
                }
            }

            override fun checkPermissionForCamera(status: Boolean, context: Context) {
                if (status) {
                    openCameraToCaptureVideo()
                    Log.d(RegistrationFragment.TAG, "checkPermissionForCamera: Success $status")
                } else {
                    Log.d(RegistrationFragment.TAG, "checkPermissionForCamera: Failure $status")
                    Toast.makeText(
                        context, "You have denied the storage to select Video",
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

    private fun selectVideoContentToUpload() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_REQUEST_CODE)
    }

    private fun openCameraToCaptureVideo() {
        if (activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true) { // First check if camera is available in the device
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent, VIDEO_CAMERA_REQUEST_CODE);
        }
    }

    private fun getExtension(uri: Uri): String? {
        val contentResolver: ContentResolver? = context?.contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver?.getType(uri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                VIDEO_REQUEST_CODE -> {
                    videoUri = data?.data
                    videoUri?.let { settingVideoToVideoView(it) }
                }

                VIDEO_CAMERA_REQUEST_CODE -> {
                    videoUri = data?.data
                    videoUri?.let { settingVideoToVideoView(it) }
                }

            }
        }
    }

    private fun settingVideoToVideoView(uri: Uri) {
        content_upload_new_video.setVideoURI(uri)
        content_upload_new_video.requestFocus()
        content_upload_new_video.start()

        val mediaController = MediaController(context)
        content_upload_new_video.setMediaController(mediaController)
        mediaController.setAnchorView(content_upload_new_video)

    }

    private fun uploadVideoToFirebase() {
        val title = content_upload_title.text.toString()
        val content = content_upload_description.text.toString()
        val dateTime = content_upload_dateTime.text.toString()

        if(videoUri != null || !TextUtils.isEmpty(title)
            || !TextUtils.isEmpty(content)) {
            content_progressBar.visibility = View.VISIBLE

            val storageReference = FirebaseStorage.getInstance().getReference("MediaPlayer")
            val reference = storageReference.child("${System.currentTimeMillis()}." +
                    "${videoUri?.let { getExtension(it) }}")
            val uploadTask = videoUri?.let { reference.putFile(it) }

            val urlTask: Task<Uri>? = uploadTask?.continueWithTask(object: Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                override fun then(task: Task<UploadTask.TaskSnapshot>): Task<Uri> {
                    if(!task.isSuccessful) {
                        throw task.exception!!
                    }
                    return reference.downloadUrl
                }

            })
                ?.addOnCompleteListener(object: OnCompleteListener<Uri> {
                    override fun onComplete(task: Task<Uri>) {
                        if(task.isSuccessful) {
                            val downloadUrl: Uri = task.result
                            content_progressBar.visibility = View.GONE
                            Toast.makeText(context, "Data Saved To Storage", Toast.LENGTH_SHORT).show()

                            mediaPlayer = MediaPlayer(downloadUrl.toString(), title, content, dateTime)
                            var id = databaseReference.push().key
                            if (id != null) {
                                databaseReference.child(id).setValue(mediaPlayer)
                            }

                        } else {
                            Toast.makeText(context, "Data Saving Fail", Toast.LENGTH_SHORT).show()
                        }
                    }

                })

        } else {
            Toast.makeText(context, "All Fields Required", Toast.LENGTH_SHORT).show()
        }

    }
}