package com.xtreme.iptv

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.xtreme.iptv.api.XtreamApi

@SuppressLint("SourceLockedOrientationActivity")
class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var buffering: ProgressBar
    private lateinit var errorTxt: TextView
    private lateinit var titleTxt: TextView
    private var streamId = 0
    private var triedTs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        buffering = findViewById(R.id.playerBuffer)
        errorTxt = findViewById(R.id.playerError)
        titleTxt = findViewById(R.id.playerTitle)
        val logo = findViewById<ImageView>(R.id.playerLogo)

        val name = intent.getStringExtra("name") ?: ""
        val url = intent.getStringExtra("url") ?: ""
        val logoUrl = intent.getStringExtra("logo") ?: ""
        streamId = intent.getIntExtra("streamId", 0)

        titleTxt.text = name
        if (logoUrl.isNotEmpty()) {
            try { Glide.with(this).load(logoUrl).into(logo) } catch (_: Exception) {}
        }

        findViewById<ImageView>(R.id.playerBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.playerFullscreen).setOnClickListener { toggleOrientation() }

        initPlayer(url)
    }

    private fun initPlayer(url: String) {
        val trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setHandleAudioBecomingNoisy(true)
            .build()

        playerView.player = player
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                buffering.visibility =
                    if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                if (state == Player.STATE_READY) errorTxt.visibility = View.GONE
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!triedTs && streamId != 0) {
                    triedTs = true
                    Toast.makeText(this@PlayerActivity, "جرب صيغة أخرى...", Toast.LENGTH_SHORT).show()
                    setMedia(XtreamApi.streamUrlTs(streamId))
                } else {
                    errorTxt.visibility = View.VISIBLE
                    errorTxt.text = "تعذر تشغيل البث\nجرب قناة أخرى"
                }
            }
        })

        setMedia(url)
    }

    private fun setMedia(url: String) {
        val dataSource = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("VLC/3.0.18 LibVLC/3.0.18")

        val mediaSource = if (url.contains(".m3u8")) {
            HlsMediaSource.Factory(dataSource)
                .createMediaSource(MediaItem.fromUri(url))
        } else {
            ProgressiveMediaSource.Factory(dataSource)
                .createMediaSource(MediaItem.fromUri(url))
        }

        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun toggleOrientation() {
        requestedOrientation =
            if (requestedOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() { super.onPause(); player?.pause() }
    override fun onResume() { super.onResume(); player?.play() }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
