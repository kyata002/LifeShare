package com.example.doan.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
        private val btnUpload: ImageView = itemView.findViewById(R.id.btn_download)

        fun bind(file: FileCloud) {
            // Bind file name
            tvName.text = file.name

            // Bind file date
            dateFile.text = "Last Modified: ${formatDate(file.lastModified)}" // Format date as needed

            // Bind file size
            sizeFile.text = "Size: ${getSizeText(file.size)}" // Format size as needed

            // Bind file type (can be used for icon or additional info)
            tvType.text = "Type: ${file.type}"

            // Set file image (You can use file.type to determine if it should be an image, PDF, etc.)
            if (file.type.contains("jpg", ignoreCase = true)) {
                // Check if the download URL is available before loading the image
                file.downloadUrl.let {
                    Picasso.get().load(it).into(imgViewFile)
                } ?: run {
                    imgViewFile.setImageResource(R.drawable.ic_file_device)  // Fallback icon if download URL is null
                }
            } else {
                // Set a default icon for non-image files
                imgViewFile.setImageResource(R.drawable.ic_file_device)
            }

            // Handle "more options" button (e.g., show file options)
            btnMore.setOnClickListener {
                // Show options for the file (e.g., download, delete)
                showFileOptions(file)
            }

            // Handle click actions for the upload button (if needed)
            btnUpload.setOnClickListener {
                // Upload logic (to be implemented)
                Toast.makeText(itemView.context, "Upload clicked for ${file.name}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun showFileOptions(file: FileCloud) {
            // Show a dialog or options to download, delete, etc.
            Toast.makeText(itemView.context, "Show options for ${file.name}", Toast.LENGTH_SHORT).show()
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
}
