package com.example.doan.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R
import com.example.doan.databinding.ActivityMainBinding
import com.example.doan.databinding.ActivitySettingBinding

@Suppress("DEPRECATION")
class SettingActivity : AppCompatActivity() {
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
            // Xử lý sự kiện cho Chia sẻ ứng dụng
            Toast.makeText(this, "Chia sẻ ứng dụng", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettingFeed .setOnClickListener {
            // Xử lý sự kiện cho Phản hồi ý kiến
            Toast.makeText(this, "Phản hồi ý kiến", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettingPri.setOnClickListener {
            // Xử lý sự kiện cho Chính sách ứng dụng
            Toast.makeText(this, "Chính sách ứng dụng", Toast.LENGTH_SHORT).show()
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
}
