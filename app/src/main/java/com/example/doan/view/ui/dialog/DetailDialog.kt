package com.example.doan.view.ui.dialog

import FileApp
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.databinding.DialogDetailBinding

@Suppress("DEPRECATION")
class DetailDialog : AppCompatActivity() {
    private lateinit var binding: DialogDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the FileApp object from the intent
        val file = intent.getSerializableExtra("FILE_DETAILS") as? FileApp

        // Display file details if available
        file?.let {
            binding.tvFileName.text = it.name
            binding.tvFileSize.text = "${it.size} bytes"
            binding.tvFileType.text = it.type
            binding.tvFilePath.text = it.path
            binding.tvLastModified.text = it.lastModified.toString()
        }
        binding.main1.setOnClickListener {
            finish()
        }
    }
}
