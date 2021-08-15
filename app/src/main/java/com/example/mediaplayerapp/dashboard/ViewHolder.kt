package com.example.mediaplayerapp.dashboard


import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayerapp.mediaplayer.MediaPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.recycler_view_items.view.*


class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    private val simpleExoPlayerView: PlayerView = view.recycler_content_videoView
    private lateinit var simpleExoPlayer: SimpleExoPlayer

    fun setExoplayer(application: Application, videoDetails: MediaPlayer) {
        val mediaPlayerThumbnail: TextView = view.recycler_content_title
        val mediaPlayerContent: TextView = view.recycler_content_description
        val mediaPlayerDateTime: TextView = view.recycler_content_dateTime

        mediaPlayerThumbnail.text = videoDetails.title
        mediaPlayerContent.text = videoDetails.content
        mediaPlayerDateTime.text = videoDetails.dateTime

        try {
            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory()
            val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(application, trackSelector)
            val videoURI = Uri.parse(videoDetails.url)

            val dataSourceFactory = DefaultHttpDataSourceFactory("MediaPlayer")
            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
            val mediaSource: MediaSource =
                ExtractorMediaSource(videoURI, dataSourceFactory, extractorsFactory, null, null)

            simpleExoPlayerView.player = simpleExoPlayer
            simpleExoPlayer.prepare(mediaSource)
            simpleExoPlayer.playWhenReady = false

        } catch (e:Exception) {
            Log.e("ViewHolder", "setExoplayer: error $e")
        }
    }
}


