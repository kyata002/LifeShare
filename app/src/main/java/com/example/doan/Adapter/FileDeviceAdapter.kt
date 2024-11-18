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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.data.model.MenuItemData
import com.example.doan.view.ui.dialog.DeleteDialog
import com.example.doan.view.ui.dialog.DetailDialog
import com.example.doan.view.ui.dialog.RenameDialog
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
                dialogMessage.text = "Tài liệu đang tải lên..."
                dialogIcon.setImageResource(R.drawable.ic_upload_filde)  // Uploading icon

                // Create and show the progress dialog
                val progressDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()
                progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                progressDialog.show()

                // Check if the file already exists in Firebase Storage
                fileRef.metadata.addOnSuccessListener {
                    // File exists, handle accordingly
                    dialogMessage.text = "Tài liệu đã tồn tại. Dừng tải lên."
                    dialogIcon.setImageResource(R.drawable.ic_warning)  // Warning icon
                    progressDialog.dismissAfterDelay(1500)
                }.addOnFailureListener {
                    // File doesn't exist, proceed to upload
                    fileRef.putFile(fileUri)
                        .addOnSuccessListener {
                            dialogMessage.text = "Tải tài liệu lên thành công!"
                            dialogIcon.setImageResource(R.drawable.ic_success)  // Success icon

                            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    val userRef = FirebaseDatabase.getInstance().getReference("users")
                                        .child(userId)

                                    userRef.child("listAppUp").addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val currentFiles = snapshot.children.toList()
                                            val nextId = currentFiles.size + 1  // Increment for the next file ID

                                            val uploadedFile = FileCloud(
                                                name = file.name,
                                                path = file.path,
                                                type = file.type,
                                                size = file.size,
                                                lastModified = file.lastModified,
                                                downloadUrl = downloadUri.toString(),
                                                fileId = nextId
                                            )

                                            userRef.child("listAppUp").child(nextId.toString())
                                                .setValue(uploadedFile)
                                                .addOnSuccessListener {
                                                    progressDialog.dismissAfterDelay(1500)
//                                                    Toast.makeText(context, "File added to listAppUp!", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    progressDialog.dismissAfterDelay(1500)
//                                                    Toast.makeText(context, "Failed to update listAppUp.", Toast.LENGTH_SHORT).show()
                                                }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            progressDialog.dismissAfterDelay(1500)
//                                            Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }.addOnFailureListener { downloadException ->
                                dialogMessage.text = "Lỗi lấy link tải: ${downloadException.message}"
                                dialogIcon.setImageResource(R.drawable.ic_failed)  // Failure icon
                                progressDialog.dismissAfterDelay(1500)
                            }
                        }
                        .addOnFailureListener { uploadException ->
                            dialogMessage.text = "Tải lên thất bại: ${uploadException.message}"
                            dialogIcon.setImageResource(R.drawable.ic_failed)  // Failure icon
                            progressDialog.dismissAfterDelay(1500)
                        }
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
            val intent = Intent(context, DeleteDialog::class.java)
            intent.putExtra("FILE_PATH", file.path)
            intent.putExtra("FILE_POSITION", position)
            (context as AppCompatActivity).startActivityForResult(intent, DELETE_FILE_REQUEST_CODE)
        }

        private fun showRenameDialog(file: FileApp, position: Int) {
            val intent = Intent(context, RenameDialog::class.java)
            intent.putExtra("FILE_PATH", file.path)
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

    companion object {
        const val DELETE_FILE_REQUEST_CODE = 1
        const val RENAME_FILE_REQUEST_CODE = 2
    }
}
