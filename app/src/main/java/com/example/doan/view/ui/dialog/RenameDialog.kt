package com.example.doan.view.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.const.Companion.ACTION_FILE_RENAME
import com.example.doan.databinding.DialogRenameBinding
import java.io.File

class RenameDialog : AppCompatActivity() {
    private lateinit var binding: DialogRenameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRenameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the file path and position from the intent
        val filePath = intent.getStringExtra("FILE_PATH")
        val position = intent.getIntExtra("FILE_POSITION", -1)

        // Pre-fill the EditText with the current file name (without extension)
        filePath?.let {
            val file = File(it)
            val fileNameWithoutExtension = file.nameWithoutExtension
            binding.etNewName.setText(fileNameWithoutExtension)
        }

        // Set onClick listener for the Cancel button
        binding.btnCancel.setOnClickListener {
            finish() // Close the dialog
        }

        // Set onClick listener for the Rename button
        binding.btnRename.setOnClickListener {
            val newName = binding.etNewName.text.toString()
            if (newName.isNotBlank()) {
                // Get the file object using the provided path
                val file = filePath?.let { File(it) }
                if (file != null && file.exists()) {
                    // Get the file extension (e.g., .txt)
                    val extension = file.extension

                    // Create the new file name with the same extension
                    val renamedFile = File(file.parent, "$newName.$extension")
                    val isRenamed = file.renameTo(renamedFile)

                    if (isRenamed) {
                        // Send broadcast to media scanner to refresh the file in the system
                        sendBroadcast(
                            Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(renamedFile)
                            )
                        )

                        // Send a broadcast that the file has been renamed
                        sendFileRenamedBroadcast(position, renamedFile.name)

                        Toast.makeText(this, "File renamed to: ${renamedFile.name}", Toast.LENGTH_SHORT).show()
                        finish() // Close the dialog
                    } else {
                        Toast.makeText(this, "Failed to rename the file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        // Optionally, dismiss the dialog if clicked outside the main area
        binding.main.setOnClickListener {
            finish()
        }
    }

    // Function to send broadcast when file is renamed
    private fun sendFileRenamedBroadcast(position: Int, newFileName: String) {
        val intent = Intent(ACTION_FILE_RENAME).apply {
            putExtra("FILE_POSITION", position)
            putExtra("NEW_FILE_NAME", newFileName)
        }
        sendBroadcast(intent)
    }
}
