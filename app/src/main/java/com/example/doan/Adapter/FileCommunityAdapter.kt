package com.example.doan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.data.model.FileCloud
import com.example.doan.databinding.ItemFileShareBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileCommunityAdapter(private var fileList: List<FileCloud>) : RecyclerView.Adapter<FileCommunityAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileShareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        // Bind file data to views using binding
        holder.binding.nameFiles.text = file.name ?: "Unknown File Name"
        holder.binding.sizeFiles.text = "Size:${getSizeText(file.size)}"
        holder.binding.dateFiles.text = "Last Modified: ${formatDate(file.lastModified)}"
        holder.binding.tvtypes.text = "Type: ${file.type}"

        setFileIcon(file, holder.binding.imgViewFiles)
    }

    override fun getItemCount(): Int = fileList.size

    fun updateData(newFileList: List<FileCloud>) {
        fileList = newFileList
        notifyDataSetChanged()
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

    class FileViewHolder(val binding: ItemFileShareBinding) : RecyclerView.ViewHolder(binding.root)
}

