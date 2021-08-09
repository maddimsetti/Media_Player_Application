package com.example.mediaplayerapp.service

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.mediaplayerapp.listeners.ImageUploadingFirebaseListener
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class ProfilePermissionService {

    fun handleUpload(bitmap: Bitmap, imageUploadingFirebaseListener: ImageUploadingFirebaseListener) {

        val boas = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val reference: StorageReference = FirebaseStorage.getInstance().reference
            .child("ProfileImages")
            .child("$uid.jpeg")

        reference.putBytes(boas.toByteArray()).addOnSuccessListener {
            imageUploadingFirebaseListener.onProfileImageUploadingToFirebase(true)
            getDownloadUrl(reference)
        }
            .addOnFailureListener {
                Log.e(TAG, "OnFailure: ", it)
                imageUploadingFirebaseListener.onProfileImageUploadingToFirebase(false)
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
            Log.d(TAG, "setUserProfileUrl: Success")
        }
            ?.addOnFailureListener {
                Log.d(TAG, "setUserProfileUrl: Failure")
            }

    }

}