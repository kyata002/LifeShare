package com.example.doan.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.const.Companion.ACTION_FILE_DELETED
import com.example.doan.databinding.DialogDeleteBinding
import java.io.File

class DeleteDialog : AppCompatActivity() {

    private lateinit var binding: DialogDeleteBinding
    private lateinit var fileToDelete: File
    private var filePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnDelete.setOnClickListener {
            val filePath = intent.getStringExtra("FILE_PATH")
            filePosition = intent.getIntExtra("FILE_POSITION", -1)

            if (filePath != null) {
                fileToDelete = File(filePath)
                if (fileToDelete.exists() && fileToDelete.delete()) {
                    // File deleted successfully
                    sendFileDeletedBroadcast()
                }
                finish()
            } else {
                finish()
            }


        }

        binding.btnCancel.setOnClickListener {
            onBackPressed()
        }

        binding.main.setOnClickListener {
            onBackPressed()
        }
    }

    private fun sendFileDeletedBroadcast() {

        val intent = Intent(ACTION_FILE_DELETED).apply {
            putExtra("FILE_POSITION", filePosition)
        }
        sendBroadcast(intent)
    }
}


