package com.example.doan.Adapter

import FileApp
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_FILE_UPLOADED
import com.example.doan.data.model.FileCloud
import com.example.doan.data.model.MenuItemData
import com.example.doan.view.ui.viewfile.PdfViewerActivity
import com.example.doan.view.ui.viewfile.ImageViewActivity
import com.example.doan.view.ui.viewfile.VideoPlayerActivity
import com.example.doan.view.ui.dialog.DeleteDialog
import com.example.doan.view.ui.dialog.DetailDialog
import com.example.doan.view.ui.dialog.RenameDialog
import com.example.doan.view.ui.viewfile.DocxViewActivity
import com.example.doan.view.ui.viewfile.ExcelViewActivity
import com.example.doan.view.ui.viewfile.TxtViewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class FileDeviceAdapter(private var files: List<FileApp>) :
    RecyclerView.Adapter<FileDeviceAdapter.FileViewHolder>() {

    lateinit var context: Context
    private val storage = FirebaseStorage.getInstance()  // Initialize FirebaseStorage instance
    private val storageRef = storage.reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        context = parent.context
        return FileViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    // Method to update the list of files
    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles(newFiles: List<FileApp>) {
        files = newFiles
        notifyDataSetChanged()  // Use DiffUtil for better performance in production
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.name_file)
        private val tvType: TextView = itemView.findViewById(R.id.tvtype)
        private val dateFile: TextView = itemView.findViewById(R.id.date_file)
        private val sizeFile: TextView = itemView.findViewById(R.id.size_file)
        private val imgViewFile: ImageView = itemView.findViewById(R.id.img_view_file)
        private val btnMore: ImageView = itemView.findViewById(R.id.more_options)
        private val btnUpload: ImageView = itemView.findViewById(R.id.btn_upload)

        // Updated to take `FileApp` instead of `File`
        fun bind(file: FileApp) {
            imgViewFile.setImageResource(getIconResource(file.type))
            tvName.text = file.name
            tvType.text = "Loại: ${file.type}"
            sizeFile.text = getSizeText(file.size)
            dateFile.text = "Thời gian: ${formatDate(file.lastModified)}"

            // Show more options menu
            btnMore.setOnClickListener { showCustomPopupMenu(it, file) }

            btnUpload.setOnClickListener {
                val fileUri = Uri.fromFile(File(file.path))
                val fileRef = storageRef.child("uploads/${file.name}")

// Inflate the custom layout for the progress dialog
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_upload_status, null)
                val dialogIcon: ImageView = dialogView.findViewById(R.id.dialog_icon)
                val dialogMessage: TextView = dialogView.findViewById(R.id.dialog_message)

// Configure the dialog content
                dialogMessage.text = "Đang tải tài liệu lên..."
                dialogIcon.setImageResource(R.drawable.ic_upload_filde)

// Create and show the progress dialog
                val progressDialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()
                progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                progressDialog.show()

// Kiểm tra nếu file đã tồn tại trong Firebase Storage
                fileRef.metadata.addOnSuccessListener {
                    // File đã tồn tại, thông báo dừng upload
                    dialogMessage.text = "File đã tồn tại. Không tải lên nữa."
                    dialogIcon.setImageResource(R.drawable.ic_warning)
                    progressDialog.dismissAfterDelay(1500)
                }.addOnFailureListener {
                    // File không tồn tại, tiếp tục tải lên
                    fileRef.putFile(fileUri)
                        .addOnSuccessListener { uploadTask ->
                            dialogMessage.text = "Tải lên thành công!"
                            dialogIcon.setImageResource(R.drawable.ic_success)

                            // File uploaded successfully, now we need to refresh the file list in the community fragment
                            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                // Get current user email, sanitize it, and get reference to Firebase
                                val sanitizedEmail = FirebaseAuth.getInstance().currentUser?.email
                                    ?.replace(".", "")?.replace("@", "")

                                if (sanitizedEmail != null) {
                                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail)

                                    userRef.child("listAppUp")
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val nextId = (snapshot.childrenCount + 1).toInt()

                                                val uploadedFile = FileCloud(
                                                    name = file.name,
                                                    size = file.size,
                                                    type = file.type,
                                                    path = file.path,
                                                    lastModified = file.lastModified,
                                                    downloadUrl = downloadUri.toString(),
                                                    location = "uploads/${file.name}",
                                                    fileId = nextId
                                                )

                                                // Add uploaded file to Firebase under listAppUp
                                                userRef.child("listAppUp").child(nextId.toString())
                                                    .setValue(uploadedFile)
                                                    .addOnSuccessListener {
                                                        dialogMessage.text = "File đã được thêm vào danh sách thành công!"
                                                        dialogIcon.setImageResource(R.drawable.ic_success)
                                                        progressDialog.dismissAfterDelay(1500)

                                                        // Refresh the list of files in the community fragment
                                                        val uploadSuccessIntent = Intent(ACTION_FILE_UPLOADED)
                                                        uploadSuccessIntent.putExtra("message", "File uploaded successfully!") // Optional data to pass
                                                        context?.sendBroadcast(uploadSuccessIntent)

                                                    }
                                                    .addOnFailureListener {
                                                        dialogMessage.text = "Lỗi: Không thể thêm file vào danh sách."
                                                        dialogIcon.setImageResource(R.drawable.ic_failed)
                                                        progressDialog.dismissAfterDelay(1500)
                                                    }
                                            }


                                            override fun onCancelled(error: DatabaseError) {
                                                dialogMessage.text = "Lỗi khi truy xuất dữ liệu: ${error.message}"
                                                dialogIcon.setImageResource(R.drawable.ic_failed)
                                                progressDialog.dismissAfterDelay(1500)
                                            }
                                        })
                                } else {
                                    dialogMessage.text = "Lỗi: Không xác định được người dùng."
                                    dialogIcon.setImageResource(R.drawable.ic_failed)
                                    progressDialog.dismissAfterDelay(1500)
                                }

                        }.addOnFailureListener { downloadException ->
                                dialogMessage.text = "Lỗi khi lấy đường dẫn tải: ${downloadException.message}"
                                dialogIcon.setImageResource(R.drawable.ic_failed)
                                progressDialog.dismissAfterDelay(1500)
                            }
                        }
                        .addOnFailureListener { uploadException ->
                            dialogMessage.text = "Tải lên thất bại: ${uploadException.message}"
                            dialogIcon.setImageResource(R.drawable.ic_failed)
                            progressDialog.dismissAfterDelay(1500)
                        }
                }
            }

            // Extension function to dismiss dialog after a delay


            itemView.setOnClickListener {
                when (file.type) {
                    "pdf" -> {
                        val intent = Intent(context, PdfViewerActivity::class.java).apply {
                            putExtra("FILE_PATH", file.path)
                        }
                        (context as AppCompatActivity).startActivity(intent)
                    }

                    "mp4" -> {
                        val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                            putExtra("FILE_PATH", file.path)
                        }
                        (context as AppCompatActivity).startActivity(intent)

                    }

                    "txt" -> {
                        val intent = Intent(context, TxtViewActivity::class.java).apply {
                            putExtra("FILE_PATH", file.path)
                        }
                        (context as AppCompatActivity).startActivity(intent)

                    }

                    "docx" -> {
//                        val intent = Intent(context, DocxViewActivity::class.java).apply {
//                            putExtra("FILE_PATH", file.path)
//                        }
//                        (context as AppCompatActivity).startActivity(intent)
                        showFileNotSupportedDialog(context)
                    }

                    "jpg", "png" -> {
                        val intent = Intent(context, ImageViewActivity::class.java).apply {
                            putExtra("FILE_PATH", file.path)
                        }
                        (context as AppCompatActivity).startActivity(intent)
                    }

                    "xlsx" -> {
//                        val intent = Intent(context, ExcelViewActivity::class.java).apply {
//                            putExtra("FILE_PATH", file.path)
//                        }
//                        (context as AppCompatActivity).startActivity(intent)
                        showFileNotSupportedDialog(context)
                    }
//                    else -> {
//                        // Handle other file types, or show an alert
//                        Toast.makeText(context, "Không hỗ trợ xem loại tệp này", Toast.LENGTH_SHORT).show()
//                    }
                }
            }


        }

        fun AlertDialog.dismissAfterDelay(delayMillis: Long) {
            Handler(Looper.getMainLooper()).postDelayed({ this.dismiss() }, delayMillis)
        }

        // Extension function to dismiss dialog after a delay
//        fun androidx.appcompat.app.AlertDialog.dismissAfterDelay(delayMillis: Long) {
//            Handler(Looper.getMainLooper()).postDelayed({
//                this.dismiss()
//            }, delayMillis)
//        }


        private fun showFileNotSupportedDialog(context: Context) {
            AlertDialog.Builder(context)
                .setTitle("File không được hỗ trợ")
                .setMessage("Định dạng tệp này chưa được hỗ trợ. Chúng tôi sẽ nâng cấp sớm nhất")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }

        private fun showCustomPopupMenu(view: View, file: FileApp) {
            val listPopupWindow = ListPopupWindow(view.context)
            val menuItems = listOf(
                MenuItemData(R.drawable.ic_share, "Chia sẻ"),
                MenuItemData(R.drawable.ic_rename, "Đổi tên"),
                MenuItemData(R.drawable.ic_delete, "Xóa bỏ"),
                MenuItemData(R.drawable.ic_detail, "Chi tiết")
            )

            val adapter = MenuAdapter(view.context, menuItems)
            listPopupWindow.anchorView = view
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.width = 300
            listPopupWindow.isModal = true

            listPopupWindow.setOnItemClickListener { _, _, position1, _ ->
                when (position1) {
                    0 -> shareFile(view.context, file)
                    1 -> showRenameDialog(file, adapterPosition)
                    2 -> showDeleteDialog(file, adapterPosition)
                    3 -> showDetailDialog(file)
                }
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }

        private fun showDetailDialog(file: FileApp) {
            val intent = Intent(context, DetailDialog::class.java).apply {
                putExtra("FILE_DETAILS", file)
            }
            (context as AppCompatActivity).startActivity(intent)
        }

        private fun showDeleteDialog(file: FileApp, position: Int) {
            val context = itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)

            val alertDialog = android.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

// Set the custom background if needed (optional)
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.transparent_background)  // Optional if you want to change the default background

// Handle button clicks
            val btnCancel = dialogView.findViewById<AppCompatTextView>(R.id.btnCancel)
            val btnDelete = dialogView.findViewById<AppCompatTextView>(R.id.btnDelete)

            btnCancel.setOnClickListener {
                alertDialog.dismiss()  // Handle cancel button action
            }

            btnDelete.setOnClickListener {
                var fileToDelete = File(file.path)
                if (fileToDelete.exists() && fileToDelete.delete()) {
                    updateFileListAfterDeletion(file)
                }
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        private fun showRenameDialog(file: FileApp, position: Int) {
            val intent = Intent(context, RenameDialog::class.java)
            intent.putExtra("FILE_PATH", file.path)
            intent.putExtra("FILE_TYPE", "DEVICE")
            intent.putExtra("FILE_POSITION", position)
            (context as AppCompatActivity).startActivityForResult(intent, RENAME_FILE_REQUEST_CODE)
        }

        private fun shareFile(context: Context, file: FileApp) {
            try {
                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
                val intentShareFile = Intent(Intent.ACTION_SEND)
                intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(file.path)))
                context.startActivity(Intent.createChooser(intentShareFile, "Chia sẻ tệp"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getIconResource(extension: String) = when (extension) {
            "mp4" -> R.drawable.ic_mp4
            "mp3" -> R.drawable.ic_mp3
            "pdf" -> R.drawable.ic_pdf
            "jpg", "jpeg", "png" -> R.drawable.ic_image
            "txt" -> R.drawable.ic_txt
            "xlsx" -> R.drawable.ic_xlsx
            else -> R.drawable.ic_unknown
        }

        private fun formatDate(lastModified: Long): String {
            return SimpleDateFormat("dd/MM/yyyy").format(Date(lastModified))
        }

        private fun getSizeText(size: Long): String {
            val sizeKB = size / 1024.0
            return "Kích thước: ${
                if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(
                    sizeKB
                )
            }"
        }
    }
    private fun updateFileListAfterDeletion(file: FileApp) {
        val position = files.indexOf(file)
        if (position >= 0) {
            // Remove the file from the file list
            files = files.toMutableList().apply { removeAt(position) }

            // Notify the adapter that an item has been removed
            notifyItemRemoved(position)
        }
    }

    companion object {
        const val DELETE_FILE_REQUEST_CODE = 1
        const val RENAME_FILE_REQUEST_CODE = 2
    }
}
