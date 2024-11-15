package com.example.doan.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.databinding.DialogDeleteBinding
import java.io.File

class DeleteDialog : AppCompatActivity() {

    private lateinit var binding: DialogDeleteBinding
    private lateinit var fileToDelete: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request for no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout
        binding = DialogDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve file path from Intent and convert it into a File object
        val filePath = intent.getStringExtra("FILE_PATH")
        if (filePath != null) {
            fileToDelete = File(filePath)
        } else {
            finish()
        }

        // Handle the delete action
        binding.btnDelete.setOnClickListener {
            if (fileToDelete.exists() && fileToDelete.delete()) {
                Log.d("DeleteDialog", "File deleted: ${fileToDelete.name}")
                setResult(RESULT_OK)  // Set result to notify the caller about the success
                finish()  // Close the dialog after deletion
            } else {
                Log.d("DeleteDialog", "Failed to delete file: ${fileToDelete.name}")
                setResult(RESULT_CANCELED)  // Notify failure
                finish()
            }
        }

        // Handle the cancel action
        binding.btnCancel.setOnClickListener {
            onBackPressed()
        }

        // Close the dialog if the user clicks outside the main area
        binding.main.setOnClickListener {
            onBackPressed()
        }
    }
}
