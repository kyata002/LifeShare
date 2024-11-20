package com.example.doan.view.ui.dialog

import FileApp
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.data.model.FileCloud
import com.example.doan.databinding.DialogDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class DetailDialog : AppCompatActivity() {
    private lateinit var binding: DialogDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileCloud = intent.getSerializableExtra("FILE_DETAILS") as? FileCloud
        val fileApp = intent.getSerializableExtra("FILE_DETAILS") as? FileApp
        val fileType = intent.getStringExtra("FILE_TYPE")


        if(fileType=="CLOUD"){
            fileCloud?.let { displayFileAppShare(it) }
        }else{
            if (fileCloud != null) {
                // Hiển thị chi tiết cho FileCloud
                displayFileCloudDetails(fileCloud)
            } else if (fileApp != null) {
                // Hiển thị chi tiết cho FileApp
                displayFileAppDetails(fileApp)
            } else {
                // Trường hợp không xác định được loại file
                binding.tvFileName.text = "Không xác định loại file"
            }
        }

        // Display file details if available


        // Đóng dialog khi nhấn nút
        binding.main1.setOnClickListener {
            finish()
        }
    }
    private fun displayFileCloudDetails(file: FileCloud) {
        binding.tvFileName.text = file.name
        binding.tvFileSize.text = getSizeText(file.size)
        binding.tvFileType.text = file.type
        binding.tvFilePath.text = file.downloadUrl
        binding.tvLastModified.text = formatDate(file.lastModified)
    }
    private fun displayFileAppDetails(file: FileApp) {
        binding.tvFileName.text = file.name
        binding.tvFileSize.text = getSizeText(file.size)
        binding.tvFileType.text = file.type
        binding.tvFilePath.text = file.path
        binding.tvLastModified.text = formatDate(file.lastModified)
    }
    private fun displayFileAppShare(file: FileCloud) {
        binding.tvFileName.text = file.name
        binding.tvFileSize.text = getSizeText(file.size)
        binding.tvFileType.text = file.type
        binding.tvFilePath.text = file.downloadUrl
        binding.tvLastModified.text = formatDate(file.lastModified)
    }
    private fun getSizeText(size: Long): String {
        val sizeKB = size / 1024.0
        return "Kích thước: ${
            if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(
                sizeKB
            )
        }"
    }
    private fun formatDate(lastModified: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lastModified))
    }
}
