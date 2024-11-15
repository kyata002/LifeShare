package com.example.doan.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.example.doan.R
import com.example.doan.databinding.ActivitySplashBinding
import com.example.doan.viewmodel.SplashViewModel

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatusBar()
        setupAnimation()
        observeViewModel()
        viewModel.startCountDown()
    }

    private fun setupAnimation() {
        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation(R.raw.loading)
        lottieAnimationView.playAnimation()
    }

    private fun observeViewModel() {
        viewModel.navigateTo.observe(this, Observer { targetActivity ->
            targetActivity?.let {
                navigateToActivity(it)
            }
        })
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        finish()
    }
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

    companion object {
        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
