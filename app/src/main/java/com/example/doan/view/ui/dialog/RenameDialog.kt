package com.example.doan.view.ui.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.const.Companion.ACTION_FILE_RENAME
import com.example.doan.data.model.FileCloud
import com.example.doan.databinding.DialogRenameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class RenameDialog : AppCompatActivity() {
    private lateinit var binding: DialogRenameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRenameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("FILE_PATH")
        val fileLoca = intent.getStringExtra("FILE_LOCA")
        val fileType = intent.getStringExtra("FILE_TYPE")
        val position = intent.getIntExtra("FILE_POSITION", -1)
        val fileId = intent.getIntExtra("FILE_ID", -1)

        if (filePath.isNullOrBlank() || fileType.isNullOrBlank()) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Hiển thị tên hiện tại
        val currentFileName = filePath.substringAfterLast("/")
        val fileNameWithoutExtension = currentFileName.substringBeforeLast(".")
        binding.etNewName.setText(fileNameWithoutExtension)

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnRename.setOnClickListener {
            val newName = binding.etNewName.text.toString().trim()
            if (newName.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập tên hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fileType == "CLOUD") {
                renameFileCloud(
                    this,
                    fileLoca.toString(),
                    "$newName.${currentFileName.substringAfterLast(".")}",
                    { onRenameSuccess(position, newName, fileId) },
                    { exception -> onRenameFailure(exception) }
                )
            } else {
                renameFileApp(filePath, newName, position)
            }
        }

        binding.main.setOnClickListener {
            finish()
        }
    }

    private fun renameFileApp(filePath: String, newName: String, position: Int) {
        val file = File(filePath)
        if (file.exists()) {
            val newFile = File(file.parent, "$newName.${file.extension}")
            if (file.renameTo(newFile)) {
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)))
                sendFileRenamedBroadcast(position, newFile.name)
                Toast.makeText(this, "Đổi tên thành công: ${newFile.name}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Không thể đổi tên", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tệp không tồn tại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renameFileCloud(
        context: Context,
        currentPath: String,
        newFileName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val oldFileRef = storage.reference.child(currentPath)
        val newFileRef = storage.reference.child(currentPath.substringBeforeLast("/") + "/" + newFileName)

        oldFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { fileData ->
            newFileRef.putBytes(fileData).addOnSuccessListener {
                oldFileRef.delete().addOnSuccessListener {
                    Toast.makeText(context, "Đổi tên thành công!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }.addOnFailureListener(onFailure)
            }.addOnFailureListener(onFailure)
        }.addOnFailureListener(onFailure)
    }

    private fun onRenameSuccess(position: Int, newName: String, fileId: Int) {
        sendFileRenamedBroadcast(position, newName)

        // Get the current user's email and sanitize it
        val sanitizedEmail = FirebaseAuth.getInstance().currentUser?.email?.replace(".", "")?.replace("@", "")

        if (sanitizedEmail != null) {
            // Reference to the user's list of uploaded files in the database
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail).child("listAppUp")

            // Try to find the file in the database by fileId
            userRef.orderByChild("fileId").equalTo(fileId.toDouble()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (fileSnapshot in snapshot.children) {
                            // Ensure fileId is present and matches
                            val currentFileId = fileSnapshot.child("fileId").getValue(Int::class.java)

                            if (currentFileId != null && currentFileId == fileId) {
                                // Create a new FileCloud object with the updated name
                                val updatedFile = fileSnapshot.getValue(FileCloud::class.java)?.apply {
                                    name = newName
                                }

                                if (updatedFile != null) {
                                    userRef.child(fileSnapshot.key!!).setValue(updatedFile)
                                        .addOnSuccessListener {
                                            // Successfully updated the file name
                                            Toast.makeText(this@RenameDialog, "Tên file đã được cập nhật", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle failure in database update
                                            Toast.makeText(this@RenameDialog, "Cập nhật thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this@RenameDialog, "Dữ liệu file không hợp lệ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@RenameDialog, "Không tìm thấy file cần cập nhật", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database read failure
                    Log.e("RenameDialog", "Database error: ${error.message}")
                    Toast.makeText(this@RenameDialog, "Lỗi khi truy xuất dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e("RenameDialog", "User email is null")
            Toast.makeText(this@RenameDialog, "Không xác định được người dùng", Toast.LENGTH_SHORT).show()
        }

        // Finish the dialog
        finish()
    }


    private fun onRenameFailure(exception: Exception) {
        Log.e("RenameDialog", "Lỗi khi đổi tên: ${exception.message}")
        Toast.makeText(this, "Đổi tên thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    private fun sendFileRenamedBroadcast(position: Int, newFileName: String) {
        val intent = Intent(ACTION_FILE_RENAME).apply {
            putExtra("FILE_POSITION", position)
            putExtra("NEW_FILE_NAME", newFileName)
        }
        sendBroadcast(intent)
    }
}
