package com.example.dleep2.disc

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dleep2.databinding.ActivityVideoBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding
    private lateinit var exoPlayer: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val videoTitle = intent.getStringExtra("VIDEO_TITLE")
        val videoDesc = intent.getStringExtra("VIDEO_DESC")
        val videoSource = intent.getStringExtra("VIDEO_SOURCE")

        setupPlayer(videoUrl)
        displayVideoDetails(videoTitle, videoDesc, videoSource)

        // Set onClickListener for back button
        binding.backarrowprivacypolicy.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupPlayer(videoUrl: String?) {
        if (videoUrl.isNullOrEmpty()) return
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        binding.ContentImage.player = exoPlayer
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun displayVideoDetails(title: String?, desc: String?, source: String?) {
        binding.ContentTitle.text = title ?: "No title"
        binding.desc.text = desc ?: "No description"
        binding.source.text = "Source: ${source ?: "Unknown"}"
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
