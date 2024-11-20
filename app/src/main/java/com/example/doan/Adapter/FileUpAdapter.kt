package com.example.doan.Adapter

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.data.model.MenuItemData
import com.example.doan.view.ui.dialog.DetailDialog
import com.example.doan.view.ui.dialog.RenameDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class FileUpAdapter(private var fileList: List<FileCloud>) :
    RecyclerView.Adapter<FileUpAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_cloud, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = fileList.size

    fun updateFileList(newFiles: List<FileCloud>) {
        fileList = newFiles
        notifyDataSetChanged()
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.name_filed)
        private val tvType: TextView = itemView.findViewById(R.id.tvtyped)
        private val dateFile: TextView = itemView.findViewById(R.id.date_filed)
        private val sizeFile: TextView = itemView.findViewById(R.id.size_filed)
        private val imgViewFile: ImageView = itemView.findViewById(R.id.img_view_filed)
        private val btnMore: ImageView = itemView.findViewById(R.id.more_optionsd)
        private val btnDownload: ImageView = itemView.findViewById(R.id.btn_download)

        fun bind(file: FileCloud) {
            tvName.text = file.name
            dateFile.text = "Last Modified: ${formatDate(file.lastModified)}"
            sizeFile.text = getSizeText(file.size)
            tvType.text = "Type: ${file.type}"
            setFileIcon(file, imgViewFile)

            btnMore.setOnClickListener { showCustomPopupMenu(it, file) }
            btnDownload.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    downloadFile(file.name, file.downloadUrl)
                }
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun downloadFile(fileName: String, fileUrl: String) {
            val context = itemView.context
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setTitle("Đang tải xuống")
            dialogBuilder.setMessage("Vui lòng chờ...")

            val progressBar = ProgressBar(context)
            dialogBuilder.setView(progressBar)
            val downloadDialog = dialogBuilder.create()
            downloadDialog.show()

            val request = DownloadManager.Request(Uri.parse(fileUrl))
            request.setTitle("Downloading $fileName")
            request.setDescription("Downloading file...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        downloadDialog.dismiss()
                        Toast.makeText(
                            context,
                            "Tải xuống $fileName thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        context?.unregisterReceiver(this)
                    }
                }
            }

            context.registerReceiver(
                onCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        private fun showCustomPopupMenu(view: View, file: FileCloud) {
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

            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> shareFile(view.context, file)
                    1 -> showRenameDialog(file)
                    2 -> showDeleteDialog(file)
                    3 -> showDetailDialog(file)
                }
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }

        private fun showDetailDialog(file: FileCloud) {
            val intent = Intent(itemView.context, DetailDialog::class.java).apply {
                putExtra("FILE_DETAILS", file)
            }
            itemView.context.startActivity(intent)
        }

        private fun getSizeText(size: Long): String {
            val sizeKB = size / 1024.0
            return if (sizeKB >= 1024) "%.2f MB".format(sizeKB / 1024) else "%.2f KB".format(sizeKB)
        }

        private fun formatDate(lastModified: Long): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lastModified))
        }

        private fun setFileIcon(file: FileCloud, imgViewFile: ImageView) {
            if (file.type.contains("jpg", true) || file.type.contains(
                    "jpeg",
                    true
                ) || file.type.contains("png", true)
            ) {
                Picasso.get().load(file.downloadUrl).into(imgViewFile)
            } else {
                imgViewFile.setImageResource(getIconResource(file.type.lowercase()))
            }
        }

        private fun getIconResource(extension: String): Int = when (extension) {
            "mp4" -> R.drawable.ic_mp4
            "mp3" -> R.drawable.ic_mp3
            "pdf" -> R.drawable.ic_pdf
            "txt" -> R.drawable.ic_txt
            "xlsx" -> R.drawable.ic_xlsx
            else -> R.drawable.ic_unknown
        }

        private fun showRenameDialog(file: FileCloud) {
            val context = itemView.context
            val intent = Intent(context, RenameDialog::class.java).apply {
                putExtra("FILE_LOCA", file.location) // Đường dẫn Firebase Storage
                putExtra("FILE_PATH", file.path) // Đường dẫn Firebase Storage
                putExtra("FILE_TYPE", "CLOUD")
                putExtra("FILE_ID", file.fileId)// Chỉ định kiểu file là Cloud
                putExtra("FILE_POSITION", adapterPosition) // Vị trí trong danh sách
            }
            context.startActivity(intent)
        }


        private fun showDeleteDialog(file: FileCloud) {
            val context = itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)

            val alertDialog = AlertDialog.Builder(context)
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
                deleteFile(file)  // Handle delete action
                alertDialog.dismiss()
            }

            alertDialog.show()


        }

        private fun deleteFile(file: FileCloud) {
            val context = itemView.context

            // 1. Delete from Firebase Storage
            val storageReference = FirebaseStorage.getInstance().reference.child(file.location)
            storageReference.delete().addOnSuccessListener {
                // 2. Delete from Firebase Realtime Database
                val sanitizedEmail = FirebaseAuth.getInstance().currentUser?.email?.replace(".", "")
                    ?.replace("@", "")
                sanitizedEmail?.let { email ->
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(email)
                        .child("listAppUp")
                    userRef.orderByChild("fileId").equalTo(file.fileId.toDouble())
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (fileSnapshot in snapshot.children) {
                                        fileSnapshot.ref.removeValue().addOnSuccessListener {
                                            // File deleted successfully
                                            updateFileListAfterDeletion(file)
                                            Toast.makeText(
                                                context,
                                                "File đã được xóa",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.addOnFailureListener { exception ->
                                            Toast.makeText(
                                                context,
                                                "Xóa file thất bại: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Không tìm thấy file trong cơ sở dữ liệu",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    context,
                                    "Lỗi khi xóa file từ cơ sở dữ liệu: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                } ?: run {
                    Toast.makeText(context, "Không thể xác định người dùng", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener { exception ->
                // Handle error during deletion from Firebase Storage
                Toast.makeText(
                    context,
                    "Xóa file thất bại từ Storage: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun updateFileListAfterDeletion(file: FileCloud) {
            val position = fileList.indexOf(file)
            if (position >= 0) {
                // Remove the file from the file list
                fileList = fileList.toMutableList().apply { removeAt(position) }

                // Notify the adapter that an item has been removed
                notifyItemRemoved(position)
            }
        }


        private fun shareFile(context: Context, file: FileCloud) {
            // Implementation for file sharing
        }
    }
}
