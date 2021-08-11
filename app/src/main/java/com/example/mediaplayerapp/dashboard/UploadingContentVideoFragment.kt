package com.example.mediaplayerapp.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.example.mediaplayerapp.R
import kotlinx.android.synthetic.main.fragment_uploading_content_video.*


class UploadingContentVideoFragment : Fragment() {

    private lateinit var uriPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uriPath = arguments?.getString("videoFullPath").toString()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uploading_content_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri: Uri = Uri.parse(uriPath)
        content_upload_new_video.setVideoURI(uri)
        content_upload_new_video.requestFocus()
        content_upload_new_video.start()

        val mediaController = MediaController(context)
        content_upload_new_video.setMediaController(mediaController)
        mediaController.setAnchorView(content_upload_new_video)

   }

}