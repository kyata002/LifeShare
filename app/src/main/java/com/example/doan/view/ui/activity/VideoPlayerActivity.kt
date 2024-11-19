package com.example.doan.view.ui.activity

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the file path from the Intent
        val filePath = intent.getStringExtra("FILE_PATH")
        if (filePath != null) {
            val videoUri = Uri.parse(filePath)
            binding.videoView.setVideoURI(videoUri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.videoView)
            binding.videoView.setMediaController(mediaController)

            binding.videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                binding.videoView.start()
            }
        } else {
            Toast.makeText(this, "Không tìm thấy đường dẫn video.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}
