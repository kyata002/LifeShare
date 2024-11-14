// FileDeviceAdapter.kt
package com.example.doan.Adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import java.io.File

class FileDeviceAdapter(private var files: List<File>) : RecyclerView.Adapter<FileDeviceAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        Log.d("FileDeviceAdapter", "Updating files: ${files.map { it.name }}")
        notifyDataSetChanged()
    }


    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.name_file)

        fun bind(file: File) {
            fileNameTextView.text = file.name
        }
    }

}
