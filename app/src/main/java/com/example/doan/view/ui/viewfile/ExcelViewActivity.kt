package com.example.doan.view.ui.viewfile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import com.example.doan.R
import com.example.doan.databinding.ActivityExcelViewBinding
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ExcelViewActivity : AppCompatActivity() {

    // Khai báo binding
    private lateinit var binding: ActivityExcelViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Thiết lập binding cho layout
        binding = ActivityExcelViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thực hiện khởi tạo giao diện tại đây
        initView()
        addEvent()
    }

    // Khởi tạo giao diện người dùng
    private fun initView() {
        val filePath = intent.getStringExtra("FILE_PATH")
        // Sử dụng binding để gán tên tệp
        binding.nameFileRead.text = File(filePath).name

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPrint.setOnClickListener {
            printPdfFile(this, Uri.fromFile(File(filePath)))
        }

        binding.btnShare.setOnClickListener {
            // AppUtils.sharefile(File(filePath), this) // Uncomment if needed
        }
    }

    // Thêm các sự kiện (nếu có)
    private fun addEvent() {
        // Không có sự kiện đặc biệt nào thêm vào trong phương thức này
    }

    // Hàm in tệp PDF
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun printPdfFile(context: Context, uri: Uri?) {
        try {
            if (PrintHelper.systemSupportsPrint()) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val sb = StringBuilder()
                sb.append(context.getString(R.string.app_name))
                sb.append(" Document")
                val sb2 = sb.toString()

//                var print = printManager.print(sb2, PrintDocumentAdapter(context, uri), null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun start(context: Context, path: String?) {
            val starter = Intent(context, ExcelViewActivity::class.java)
            starter.putExtra("FILE_PATH", path)
            context.startActivity(starter)
        }
    }
}
