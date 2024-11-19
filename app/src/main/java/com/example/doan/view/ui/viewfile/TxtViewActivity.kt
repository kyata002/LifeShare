package com.example.doan.view.ui.viewfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.R
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset

class TxtViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txt_view)

        val filePath = intent.getStringExtra("FILE_PATH")
        val textView: TextView = findViewById(R.id.text_view)  // TextView để hiển thị nội dung tệp
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { finish() }
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists() && file.extension.equals("txt", ignoreCase = true)) {
                try {
                    val fileContent = readTextFile(file)
                    textView.text = fileContent
                } catch (e: IOException) {
                    showErrorDialog("Không thể đọc tệp.")
                }
            } else {
                showErrorDialog("Đây không phải là tệp .txt.")
            }
        }
    }

    // Đọc nội dung của tệp .txt
    private fun readTextFile(file: File): String {
        val fileInputStream = FileInputStream(file)
        val content = fileInputStream.bufferedReader(Charset.defaultCharset()).use { it.readText() }
        fileInputStream.close()
        return content
    }

    // Hiển thị Dialog lỗi
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        fun start(context: Context, path: String) {
            val intent = Intent(context, TxtViewActivity::class.java).apply {
                putExtra("FILE_PATH", path)
            }
            context.startActivity(intent)
        }
    }
}
