package com.example.mediaplayerapp.listeners

import android.content.Context

interface PermissionsListener {

    fun checkPermissionForCamera(status: Boolean, context: Context)
    fun checkPermissionForGallery(status: Boolean, context: Context)

}