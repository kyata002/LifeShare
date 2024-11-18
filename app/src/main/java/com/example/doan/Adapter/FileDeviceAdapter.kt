package com.example.doan.Adapter

import FileApp
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.const.Companion.BASE_URL
import com.example.doan.data.model.MenuItemData
import com.example.doan.service.UploadFileToCloudfy
import com.example.doan.view.ui.dialog.DeleteDialog
import com.example.doan.view.ui.dialog.DetailDialog
import com.example.doan.view.ui.dialog.RenameDialog
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class FileDeviceAdapter(private var files: List<FileApp>) :
    RecyclerView.Adapter<FileDeviceAdapter.FileViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        context = parent.context
        return FileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file, parent, false))
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
                val apiKey = "YOUR_API_KEY"  // Replace with your actual API key
//                val baseUrl = "https://example.com/api/"  // Replace with the actual base URL
                UploadFileToCloudfy.uploadFileToCloudfy(context, fileUri, apiKey, BASE_URL)
            }
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
            return "Kích thước: ${if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(sizeKB)}"
        }
    }

    companion object {
        const val DELETE_FILE_REQUEST_CODE = 1
        const val RENAME_FILE_REQUEST_CODE = 2
    }
}
