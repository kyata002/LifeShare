package com.example.doan.Adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.const.Companion.PATTERN_FORMAT_DATE
import com.example.doan.model.MenuItemData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class FileDeviceAdapter(private var files: List<File>) :
    RecyclerView.Adapter<FileDeviceAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) = holder.bind(files[position])

    override fun getItemCount() = files.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        Log.d("FileDeviceAdapter", "Updating files: ${files.map { it.name }}")
        notifyDataSetChanged()
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.name_file)
        private val tvType: TextView = itemView.findViewById(R.id.tvtype)
        private val dateFile: TextView = itemView.findViewById(R.id.date_file)
        private val sizeFile: TextView = itemView.findViewById(R.id.size_file)
        private val imgviewfile: ImageView = itemView.findViewById(R.id.img_view_file)
        private val btnMore: ImageView = itemView.findViewById(R.id.more_options)
        private val btnUpload:ImageView = itemView.findViewById(R.id.more_options)

        @SuppressLint("SetTextI18n")
        fun bind(file: File) {
            imgviewfile.setImageResource(getIconResource(file.extension))
            tvName.text = file.name
            tvType.text = "Loại: ${file.extension}"
            sizeFile.text = getSizeText(file)
            dateFile.text = "Thời gian: ${formatDate(file)}"
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
            listPopupWindow.width = 300 // Set custom width here
            listPopupWindow.isModal = true

            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> Log.d("FileViewHolder", "Open file: ${file.name}")
                    1 -> Log.d("FileViewHolder", "Rename file: ${file.name}")
                    2 -> Log.d("FileViewHolder", "Delete file: ${file.name}")
                    3 -> Log.d("FileViewHolder", "View details of file: ${file.name}")
                }
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }




        private fun getIconResource(extension: String) = when (extension) {
            "mp4" -> R.drawable.ic_mp4
            "mp3" -> R.drawable.ic_mp3
            "pdf" -> R.drawable.ic_pdf
            "jpg" -> R.drawable.ic_image
            "txt" -> R.drawable.ic_txt
            "xlsx" -> R.drawable.ic_xlsx
            else -> R.drawable.ic_unknown
        }

        @SuppressLint("SimpleDateFormat")
        private fun formatDate(file: File) = SimpleDateFormat(PATTERN_FORMAT_DATE).format(Date(file.lastModified()))

        private fun getSizeText(file: File): String {
            val sizeKB = file.length() / 1024.0
            return "Size: ${if (sizeKB >= 1024) "%.2fMB".format(sizeKB / 1024) else "%.2fKB".format(sizeKB)}"
        }
    }
}
