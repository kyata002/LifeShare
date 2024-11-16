package com.example.doan.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_FILE_DELETED
import com.example.doan.const.Companion.ACTION_FILE_RENAME
import com.example.doan.model.MenuItemData
import com.example.doan.ui.dialog.DeleteDialog
import com.example.doan.ui.dialog.RenameDialog
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class FileDeviceAdapter(private var files: List<File>) :
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
    fun updateFiles(newFiles: List<File>) {
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

        fun bind(file: File) {
            imgViewFile.setImageResource(getIconResource(file.extension))
            tvName.text = file.name
            tvType.text = "Loại: ${file.extension}"
            sizeFile.text = getSizeText(file)
            dateFile.text = "Thời gian: ${formatDate(file)}"

            // Show more options menu
            btnMore.setOnClickListener { showCustomPopupMenu(it, file) }
        }

        private fun showCustomPopupMenu(view: View, file: File) {
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
                    1 -> showRenameDialog(file,position)
                    2 -> showDeleteDialog(file,position)
                    3 -> Log.d("FileViewHolder", "View details of file: ${file.name}")
                }
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }

        private fun showDeleteDialog(file: File, position: Int) {
            val intent = Intent(context, DeleteDialog::class.java)
            intent.putExtra("FILE_PATH", file.absolutePath)
            intent.putExtra("FILE_POSITION", position)
            (context as AppCompatActivity).startActivityForResult(intent, DELETE_FILE_REQUEST_CODE)
        }
        private fun showRenameDialog(file: File, position: Int) {
            val intent = Intent(context, RenameDialog::class.java)
            intent.putExtra("FILE_PATH", file.absolutePath)
            intent.putExtra("FILE_POSITION", position)
            (context as AppCompatActivity).startActivityForResult(intent, RENAME_FILE_REQUEST_CODE)
        }


        private fun shareFile(context: Context, file: File) {
            try {
                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())
                val intentShareFile = Intent(Intent.ACTION_SEND)
                intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
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

        private fun formatDate(file: File): String {
            return SimpleDateFormat("dd/MM/yyyy").format(Date(file.lastModified()))
        }

        private fun getSizeText(file: File): String {
            val sizeKB = file.length() / 1024.0
            return "Kích thước: ${if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(sizeKB)}"
        }
    }


    companion object {
        const val DELETE_FILE_REQUEST_CODE = 1
        const val RENAME_FILE_REQUEST_CODE = 2
    }
}
