package com.example.doan.Adapter

import android.annotation.SuppressLint
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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.databinding.ItemFileShareBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileShareFileAdapter(private var fileList: List<FileCloud>) : RecyclerView.Adapter<FileShareFileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileShareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        val context = holder.binding.root.context

        // Bind file data to views using binding
        holder.binding.nameFiles.text = file.name ?: "Unknown File Name"
        holder.binding.sizeFiles.text = "Dung lượng: ${getSizeText(file.size)}"
        holder.binding.dateFiles.text = "Thời gian: ${formatDate(file.lastModified)}"
        holder.binding.tvtypes.text = "Loại: ${file.type}"

        holder.binding.btnDownloads.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                file.name?.let { fileName ->
                    downloadFile(context, fileName, file.downloadUrl)
                } ?: Toast.makeText(context, "File name not available", Toast.LENGTH_SHORT).show()
            }
        }

        setFileIcon(context, file, holder.binding.imgViewFiles)
    }

    private fun downloadFile(context: Context, fileName: String, fileUrl: String) {
        val dialogBuilder = AlertDialog.Builder(context)
            .setTitle("Đang tải xuống")
            .setMessage("Vui lòng chờ...")
            .setCancelable(false)

        val progressBar = ProgressBar(context)
        dialogBuilder.setView(progressBar)
        val downloadDialog = dialogBuilder.create()
        downloadDialog.show()

        val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle("Downloading $fileName")
            setDescription("Downloading file...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    downloadDialog.dismiss()
                    Toast.makeText(context, "Tải xuống $fileName thành công", Toast.LENGTH_SHORT).show()
                    try {
                        context?.unregisterReceiver(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        context.registerReceiver(onCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun getItemCount(): Int = fileList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles(newFiles: List<FileCloud>) {
        fileList = newFiles
        notifyDataSetChanged()  // Use DiffUtil for better performance in production
    }

    private fun getSizeText(size: Long): String {
        val sizeKB = size / 1024.0
        return if (sizeKB >= 1024) "%.2f MB".format(sizeKB / 1024) else "%.2f KB".format(sizeKB)
    }

    private fun formatDate(lastModified: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lastModified))
    }

    private fun setFileIcon(context: Context, file: FileCloud, imgViewFile: ImageView) {
        if (file.type.contains("jpg", true) || file.type.contains("jpeg", true) || file.type.contains("png", true)) {
            Picasso.get()
                .load(file.downloadUrl)
                .error(R.drawable.ic_warning) // Fallback image in case of error
                .into(imgViewFile)
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

    class FileViewHolder(val binding: ItemFileShareBinding) : RecyclerView.ViewHolder(binding.root)
}
