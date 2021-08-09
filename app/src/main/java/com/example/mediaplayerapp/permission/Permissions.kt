package com.example.mediaplayerapp.permission

import android.content.Context
import com.example.mediaplayerapp.listeners.PermissionsListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class Permissions {

    fun galleryCheckPermission(context: Context, listener: PermissionsListener) {

        Dexter.withContext(context).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                listener.checkPermissionForGallery(true, context)
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                listener.checkPermissionForGallery(false, context)
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                listener.checkPermissionForGallery(false, context)
            }
        }).onSameThread().check()
    }

    fun cameraCheckPermission(context: Context, listener: PermissionsListener) {

        Dexter.withContext(context).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                           listener.checkPermissionForCamera(true, context)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    listener.checkPermissionForCamera(true, context)
                }
            }
        ).onSameThread().check()
    }

}

