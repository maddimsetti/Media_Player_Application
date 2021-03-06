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
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.listeners.DashBoardListener
import com.example.mediaplayerapp.listeners.ImageUploadingFirebaseListener
import com.example.mediaplayerapp.listeners.PermissionsListener
import com.example.mediaplayerapp.mediaplayer.MediaPlayer
import com.example.mediaplayerapp.permission.Permissions
import com.example.mediaplayerapp.registration.MainActivity
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.example.mediaplayerapp.service.DashBoardService
import com.example.mediaplayerapp.service.ProfilePermissionService
import com.example.mediaplayerapp.uripath.*
import com.firebase.ui.database.paging.DatabasePagingOptions
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter
import com.firebase.ui.database.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import kotlinx.android.synthetic.main.dialog_box_logout.view.*
import kotlinx.android.synthetic.main.dialog_forgot_password.view.*
import kotlinx.android.synthetic.main.dialog_profile_details.*
import kotlinx.android.synthetic.main.dialog_profile_details.view.*
import kotlinx.android.synthetic.main.fragment_uploading_content_video.*
import kotlinx.android.synthetic.main.recycler_view_items.*
import kotlinx.android.synthetic.main.recycler_view_items.view.*
import kotlinx.android.synthetic.main.toolbar_profile.*
import kotlinx.android.synthetic.main.toolbar_profile.view.*

class DashBoardActivity : AppCompatActivity() {
    private lateinit var dashBoardService: DashBoardService
    private lateinit var profilePermissionService: ProfilePermissionService
    private lateinit var permissionForCaptureImage: Permissions

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 2001

    private lateinit var view: View
    private lateinit var profileImage: CircleImageView

    private lateinit var mAuthentication: FirebaseAuth //Firebase Authentication
    private lateinit var currentUser: FirebaseUser //currentUser Initializing

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        bottomNavigationView.background = null
        bottomNavigationView.selectedItemId = R.id.navigation_home
        onNavigationItemSelected()

        setSupportActionBar(custom_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        custom_toolbar.showOverflowMenu()

        mAuthentication = FirebaseAuth.getInstance()
//        currentUser = mAuthentication.currentUser!!

        dashBoardService = DashBoardService() //Initialize the DashBoard Service
        profilePermissionService =
            ProfilePermissionService() //Initialize the ProfilePermissionService

        permissionForCaptureImage = Permissions() //permissions class
        view = layoutInflater.inflate(R.layout.dialog_profile_details, null)
        profileImage = view.profile_details_image as CircleImageView

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("MediaPlayer List")
        mediaPlayer = MediaPlayer()

        swipeRefreshLayout = swipe_refresh_layout_page

        recyclerView = dashBoard_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this@DashBoardActivity)

    }

    override fun onStart() {
        super.onStart()
//        updateUI(currentUser)

        //Initialize PagedList Configuration
        val configuration = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(5)
            .setPageSize(10)
            .build()

        //Initialize FirebasePagingOptions
        val firebaseOptions: DatabasePagingOptions<MediaPlayer> =
            DatabasePagingOptions.Builder<MediaPlayer>()
                .setLifecycleOwner(this@DashBoardActivity)
                .setQuery(databaseReference, configuration, MediaPlayer::class.java).build()

        //Initialize Adapter
        val firebaseAdapter: FirebaseRecyclerPagingAdapter<MediaPlayer, ViewHolder> = object :
            FirebaseRecyclerPagingAdapter<MediaPlayer, ViewHolder>(firebaseOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_items, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ViewHolder,
                position: Int,
                mediaPlayer: MediaPlayer
            ) {
                holder.setExoplayer(application, mediaPlayer)
                holder.itemView.setOnClickListener() {
                    val intent = Intent(this@DashBoardActivity, FullScreenActivity::class.java)
                    intent.putExtra("Title", mediaPlayer.title)
                    intent.putExtra("Content", mediaPlayer.content)
                    intent.putExtra("DateTime", mediaPlayer.dateTime)
                    intent.putExtra("Url", mediaPlayer.url)
                    startActivity(intent)

                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }

                    LoadingState.LOADED -> {

                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.LOADING_MORE -> {
                        swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.ERROR -> {
                        retry()
                    }
                }
            }

            override fun onError(databaseError: DatabaseError) {
                super.onError(databaseError)
                swipeRefreshLayout.isRefreshing = false
                databaseError.toException().printStackTrace()
            }

        }
        firebaseAdapter.notifyDataSetChanged()
        firebaseAdapter.startListening()
        recyclerView.adapter = firebaseAdapter

        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                firebaseAdapter.refresh()
            }

        })
    }

//    private fun updateUI(currentUser: FirebaseUser?) {
//        if (currentUser?.photoUrl != null) {
//            Picasso.with(this@DashBoardActivity).load(currentUser.photoUrl).into(profileImage)
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_action_items, menu)

        val menuItem = menu?.findItem(R.id.toolbar_profileImage)
        val view: View = MenuItemCompat.getActionView(menuItem)

        val profile = view.toolbar_profile_image
//        if (currentUser?.photoUrl != null) {
//            Picasso.with(this@DashBoardActivity).load(currentUser.photoUrl).into(profile)
//        }

        profile.setOnClickListener {
            viewUserProfileDetails()

            Toast.makeText(this@DashBoardActivity, "Profile Image Selected", Toast.LENGTH_SHORT)
                .show()
        }


        val menuSearchItem = menu?.findItem(R.id.toolbar_search)
        val searchView = MenuItemCompat.getActionView(menuSearchItem) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    firebaseSearch(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    firebaseSearch(newText)
                }
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.toolbar_upload_videos -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.dashBoard_fragment_container, UploadingContentVideoFragment())
                    .commit()
                Toast.makeText(this@DashBoardActivity, "content Upload", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun firebaseSearch(searchText: String) {
        val query = searchText.toLowerCase()
        val firebaseQuery: Query =
            databaseReference.orderByChild("title").startAt(query).endAt(query + "\uf8ff")

        val configuration = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(5)
            .setPageSize(10)
            .build()

        //Initialize FirebasePagingOptions
        val firebaseOptions: DatabasePagingOptions<MediaPlayer> =
            DatabasePagingOptions.Builder<MediaPlayer>()
                .setLifecycleOwner(this@DashBoardActivity)
                .setQuery(firebaseQuery, configuration, MediaPlayer::class.java).build()

        //Initialize Adapter
        val firebaseAdapter: FirebaseRecyclerPagingAdapter<MediaPlayer, ViewHolder> = object :
            FirebaseRecyclerPagingAdapter<MediaPlayer, ViewHolder>(firebaseOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_items, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ViewHolder,
                position: Int,
                mediaPlayer: MediaPlayer
            ) {
                holder.setExoplayer(application, mediaPlayer)
                holder.itemView.setOnClickListener() {
                    val intent = Intent(this@DashBoardActivity, FullScreenActivity::class.java)
                    intent.putExtra("Title", mediaPlayer.title)
                    intent.putExtra("Content", mediaPlayer.content)
                    intent.putExtra("DateTime", mediaPlayer.dateTime)
                    intent.putExtra("Url", mediaPlayer.url)
                    startActivity(intent)

                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }

                    LoadingState.LOADED -> {
                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.LOADING_MORE -> {
                        swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.ERROR -> {
                        retry()
                    }
                }
            }

            override fun onError(databaseError: DatabaseError) {
                super.onError(databaseError)
                swipeRefreshLayout.isRefreshing = false
                databaseError.toException().printStackTrace()
            }

        }
        firebaseAdapter.notifyDataSetChanged()
        firebaseAdapter.startListening()
        recyclerView.adapter = firebaseAdapter

        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                firebaseAdapter.refresh()
            }

        })

    }

    private fun viewUserProfileDetails() {
        val builder = AlertDialog.Builder(this@DashBoardActivity)

        dashBoardService.gettingUserProfileDetailsFromFirebase(profileListener)
        profileImage.setOnClickListener() {
            selectingImage()
        }
        builder.setView(view)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        view.profile_details_OkayBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
//        view.dialog_profile_details.removeView(view)

    }

    private val profileListener: DashBoardListener = object : DashBoardListener {
        override fun onRetrieveProfileDetailsFromFirebase(
            status: Boolean,
            fullName: String?, eMailID: String?, phoneNumber: String?
        ) {
            if (status) {
                Log.d(TAG, "onRetrieveProfileDetailsFromFirebase: Success $status")
                view.profile_details_name.text = fullName
                view.profile_details_email.text = eMailID
                view.profile_details_phone.text = phoneNumber

            } else {
                Toast.makeText(
                    this@DashBoardActivity, "Fetching the Data From Firebase Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun selectingImage() {
        val pictureDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItem =
            arrayOf("Select photo from Gallery", "Capture Photo from Camera")
        pictureDialog.setItems(pictureDialogItem) { _, selection ->
            when (selection) {
                0 -> permissionForCaptureImage.galleryCheckPermission(
                    this@DashBoardActivity,
                    permissionListeners
                )
                1 -> permissionForCaptureImage.cameraCheckPermission(
                    this@DashBoardActivity,
                    permissionListeners
                )
            }
        }
        pictureDialog.show()
    }

    private val permissionListeners: PermissionsListener = object : PermissionsListener {

        override fun checkPermissionForGallery(status: Boolean, context: Context) {
            if (status) {
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
            if (status) {
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
        profileImage.setImageBitmap(bitmap)
        if (bitmap != null) {
            profilePermissionService.handleUpload(bitmap, uploadingImageListener)
        }
    }

    private val uploadingImageListener: ImageUploadingFirebaseListener =
        object : ImageUploadingFirebaseListener {
            override fun onProfileImageUploadingToFirebase(status: Boolean) {
                if (status) {
                    Toast.makeText(
                        this@DashBoardActivity, "Uploading and Setting Profile Succeeded $status",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@DashBoardActivity, "Failed Uploading and Setting Profile $status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun onNavigationItemSelected() {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    Toast.makeText(this@DashBoardActivity, "Home Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.navigation_notification -> {
                    Toast.makeText(this@DashBoardActivity, "Notification", Toast.LENGTH_SHORT).show()
                }

                R.id.navigation_logOut -> {
                    layOut()
                }
            }
            true
        }
    }

    fun layOut() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_box_logout, null)
        view.dialog_signOut
        builder.setView(view)
        builder.setPositiveButton("Confirm") { _, _ ->
            signOut()
        }
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.show()

    }

    private fun signOut() {
        mAuthentication.signOut()
        Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@DashBoardActivity, MainActivity::class.java))
        finish()
    }

}