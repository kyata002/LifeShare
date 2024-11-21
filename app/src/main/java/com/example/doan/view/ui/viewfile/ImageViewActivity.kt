package com.example.doan.view.ui.viewfile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.doan.databinding.ActivityImageViewBinding
import java.io.File

class ImageViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val filePath = intent.getStringExtra("FILE_PATH")

        if (filePath != null) {
            val imageFile = File(filePath)
            if (imageFile.exists()) {
                // Load the image into the ImageView
                binding.imgView.setImageURI(imageFile.toUri())
            } else {
                Toast.makeText(this, "Hình ảnh không tồn tại.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Đường dẫn tài liệu không đúng.", Toast.LENGTH_SHORT).show()
        }

        // Handle back button click
        binding.btnBackImageView.setOnClickListener {
            finish() // Close the activity
        }
    }
}