package com.example.mediaplayerapp.service

import android.util.Log
import android.widget.Toast
import com.example.mediaplayerapp.listeners.DashBoardListener
import com.example.mediaplayerapp.pojoclass.ProfileDetails
import com.example.mediaplayerapp.registration.RegistrationFragment.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dialog_profile_details.view.*

class DashBoardService {

    fun gettingUserProfileDetailsFromFirebase(
        userProfileDetails: ProfileDetails?, listener: DashBoardListener) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val firebaseReference = FirebaseDatabase.getInstance().getReference("User Details")
        var userID = firebaseUser?.uid

        if (userID != null) {
            firebaseReference.child(userID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val userProfileDetails: ProfileDetails? =
//                            snapshot.getValue(ProfileDetails::class.java)
                        snapshot.getValue(ProfileDetails::class.java)
                        if (userProfileDetails != null) {
                            listener.onRetrieveProfileDetailsFromFirebase(true)

                            userProfileDetails.fullName
                            val userEmailID = userProfileDetails.email
                            val userPhoneNumber = userProfileDetails.phoneNumber

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listener.onRetrieveProfileDetailsFromFirebase(false)
                        Log.w(TAG, "onCancelled: Failed")
                    }

                })
        }
    }
}