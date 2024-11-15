package com.example.doan.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.databinding.ActivitySettingBinding
import java.io.File
import java.net.URLConnection

@Suppress("DEPRECATION")
class SettingActivity : AppCompatActivity() {

    val POLICY_URL: String = "https://firebasestorage.googleapis.com/v0/b/compass-app-df4f4.appspot.com/o/Privacy_Policy_ExcelReader.html?alt=media&token=fcb1f0bf-52ce-4b87-b845-b59f8ee0f9d8"

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatusBar()

        binding.btnSettingRate.setOnClickListener {
            // Xử lý sự kiện cho Đánh giá ứng dụng
            Toast.makeText(this, "Đánh giá ứng dụng", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettingShare.setOnClickListener {
            shareApp(this)
        }

        binding.btnSettingFeed .setOnClickListener {
            // Xử lý sự kiện cho Phản hồi ý kiến
            Toast.makeText(this, "Phản hồi ý kiến", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettingPri.setOnClickListener {
            showPolicy(this)
        }
        binding.btnSettingBack.setOnClickListener {
            onBackPressed()
        }
    }
    @SuppressLint("ObsoleteSdkInt")
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Đặt nền của thanh trạng thái
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)

            // Đặt màu biểu tượng (tối hoặc sáng)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true // hoặc false cho biểu tượng sáng
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
        }
    }
    fun shareApp(context: Context) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        val shareBody =
            "https://play.google.com/store/apps/details?id=" + context.packageName
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        context.startActivity(Intent.createChooser(sharingIntent, "Share to"))
    }
    fun showPolicy(context: Context?) {
        context?.let { openWeb(it, POLICY_URL) }
    }
    fun openWeb(context: Context, url: String?) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        url
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
