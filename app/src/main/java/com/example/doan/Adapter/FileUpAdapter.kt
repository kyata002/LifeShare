package com.example.doan.Adapter

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date

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

    // Method to update the list in an immutable way
    fun updateFileList(newFiles: List<FileCloud>) {
        fileList = newFiles  // Reassign the new list
        notifyDataSetChanged()  // Notify adapter to update the list
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
            // Bind file name
            tvName.text = file.name

            // Bind file date
            dateFile.text =
                "Last Modified: ${formatDate(file.lastModified)}" // Format date as needed

            // Bind file size
            sizeFile.text = "Size: ${getSizeText(file.size)}" // Format size as needed

            // Bind file type (can be used for icon or additional info)
            tvType.text = "Type: ${file.type}"

            // Set file image (You can use file.type to determine if it should be an image, PDF, etc.)
            setFileIcon(file, imgViewFile)

            // Handle "more options" button (e.g., show file options)
            btnMore.setOnClickListener {
                // Show options for the file (e.g., download, delete)
                showFileOptions(file)
            }

            // Handle click actions for the upload button (if needed)
            btnDownload.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    downloadFile(file.name, file.downloadUrl)
                }
            }

        }

        private fun showFileOptions(file: FileCloud) {
            // Show a dialog or options to download, delete, etc.
            Toast.makeText(itemView.context, "Show options for ${file.name}", Toast.LENGTH_SHORT)
                .show()
        }

        private fun formatDate(lastModified: Long): String {
            return SimpleDateFormat("dd/MM/yyyy").format(Date(lastModified))
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun downloadFile(fileName: String, fileUrl: String) {
            // Hiện dialog với ProgressBar
            val dialogBuilder = AlertDialog.Builder(itemView.context)
            dialogBuilder.setTitle("Đang tải xuống")
            dialogBuilder.setMessage("Vui lòng chờ...")

            // Thêm ProgressBar vào dialog
            val progressBar = ProgressBar(itemView.context)
            dialogBuilder.setView(progressBar)
            val downloadDialog = dialogBuilder.create()
            downloadDialog.show()

            // Tạo yêu cầu tải xuống
            val request = DownloadManager.Request(Uri.parse(fileUrl))
            request.setTitle("Downloading $fileName")
            request.setDescription("Downloading file...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // Lấy DownloadManager và thực hiện download
            val downloadManager =
                itemView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // Lắng nghe sự kiện hoàn tất tải xuống
            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        // Tải xuống hoàn tất, đóng dialog
                        downloadDialog.dismiss()
                        Toast.makeText(
                            itemView.context,
                            "Tải xuống $fileName thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Hủy đăng ký receiver để tránh rò rỉ bộ nhớ
                        itemView.context.unregisterReceiver(this)
                    }
                }
            }

            // Đăng ký receiver
            itemView.context.registerReceiver(
                onCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        }


        private fun getSizeText(size: Long): String {
            val sizeKB = size / 1024.0
            return "Kích thước: ${
                if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(
                    sizeKB
                )
            }"
        }
        // Function to set image or icon based on file type
        private fun setFileIcon(file: FileCloud, imgViewFile: ImageView) {
            if (file.type.contains("jpg", ignoreCase = true) || file.type.contains("jpeg", ignoreCase = true) || file.type.contains("png", ignoreCase = true)) {
                // Check if the download URL is available before loading the image
                file.downloadUrl?.let {
                    Picasso.get().load(it).into(imgViewFile)
                } ?: run {
                    imgViewFile.setImageResource(R.drawable.ic_file_device)  // Fallback icon if download URL is null
                }
            } else {
                // Set an icon based on the file extension for non-image files
                val extension = file.type.lowercase()
                val iconResource = getIconResource(extension)
                imgViewFile.setImageResource(iconResource)
            }
        }

        // Function to map file extensions to icons
        private fun getIconResource(extension: String): Int = when (extension) {
            "mp4" -> R.drawable.ic_mp4
            "mp3" -> R.drawable.ic_mp3
            "pdf" -> R.drawable.ic_pdf
            "txt" -> R.drawable.ic_txt
            "xlsx" -> R.drawable.ic_xlsx
            else -> R.drawable.ic_unknown
        }

    }
}
