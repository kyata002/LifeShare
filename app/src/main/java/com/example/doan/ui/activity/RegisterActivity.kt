package com.example.doan.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupStatusBar()
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