package com.example.doan.view.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.R

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
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