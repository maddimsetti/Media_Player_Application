package com.example.mediaplayerapp.listeners

interface DashBoardListener {
    fun onRetrieveProfileDetailsFromFirebase(
        status: Boolean, fullName: String?, eMailID: String?, phoneNumber: String?)
}