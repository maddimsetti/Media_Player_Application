package com.example.mediaplayerapp.dashboard

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mediaplayerapp.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_full_screen.*
import kotlinx.android.synthetic.main.exoplayer_controller_custom_layout.*
import kotlinx.android.synthetic.main.exoplayer_controller_custom_layout.view.*
import kotlinx.android.synthetic.main.fullscreen_toolbar.*

class FullScreenActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var simpleExoPlayerView: PlayerView
    private var exoPlayer: SimpleExoPlayer? = null

    private var url: String? = null
    private lateinit var fullScreenButton: ImageView
    var fullScreen = false
    private var playWhenReady = false
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        setSupportActionBar(fullscreen_view_toolbar)

        simpleExoPlayerView = fullScreen_exoplayer
        fullScreenButton = simpleExoPlayerView.exoplayer_fullscreen_icon

        val intent = intent
        fullscreen_exo_title.text = intent.extras?.getString("Title")
        fullscreen_exo_description.text = intent.extras?.getString("Content")
        fullscreen_exo_dateTime.text = intent.extras?.getString("DateTime")
        url = intent.extras?.getString("Url")

        fullScreenButton.setOnClickListener() {
            if (fullScreen) {
                setScreenToPortrait()
            } else {
                setScreenToLandScape()
            }
        }
        fullScreen_backButton.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fullScreen_backButton -> {
                exoPlayer?.stop()
                releasePlayer()
                val intent = Intent(this@FullScreenActivity, DashBoardActivity::class.java)
                startActivity(intent)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }


    private fun setScreenToPortrait() {
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this@FullScreenActivity,
                R.drawable.exo_controls_fullscreen_enter
            )
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        supportActionBar?.show()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val params = simpleExoPlayerView.layoutParams as LinearLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = (200 * applicationContext.resources.displayMetrics.density).toInt()
        simpleExoPlayerView.layoutParams = params
        fullScreen = false
        fullscreen_exo_title.visibility = View.VISIBLE
        fullscreen_exo_description.visibility = View.VISIBLE
        fullscreen_exo_dateTime.visibility = View.VISIBLE

    }

    private fun setScreenToLandScape() {
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this@FullScreenActivity,
                R.drawable.exo_controls_fullscreen_exit
            )
        )

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val params = simpleExoPlayerView.layoutParams as LinearLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        simpleExoPlayerView.layoutParams = params
        fullScreen = true
        fullscreen_exo_title.visibility = View.INVISIBLE
        fullscreen_exo_description.visibility = View.INVISIBLE
        fullscreen_exo_dateTime.visibility = View.INVISIBLE

    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory("MediaPlayer List")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun startingExoPlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this@FullScreenActivity)
        simpleExoPlayerView.player = exoPlayer
        val uri: Uri = Uri.parse(url)
        val mediaSource = buildMediaSource(uri)
        exoPlayer!!.playWhenReady = playWhenReady
        exoPlayer!!.seekTo(currentWindow, playBackPosition)
        exoPlayer!!.prepare(mediaSource, false, false)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 26) {
            startingExoPlayer()
        }
    }

    private fun releasePlayer() {
        exoPlayer?.let {
            playWhenReady = it.playWhenReady
            playBackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            exoPlayer = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 26) {
            releasePlayer()
        }
    }
}