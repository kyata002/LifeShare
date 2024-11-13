package com.example.doan.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.doan.Adapter.MainPagerAdapter
import com.example.doan.R
import com.example.doan.databinding.ActivityMainBinding
import com.example.doan.ui.activity.LoginActivity
import com.example.doan.viewmodel.ViewModelMain
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ViewModelMain by viewModels()

    // SharedPreferences để lưu trạng thái đăng nhập
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPagerWithTabs()
    }

    private fun setupViewPagerWithTabs() {
        val adapter = MainPagerAdapter(this) {
            // Kiểm tra đăng nhập và chỉ chuyển hướng khi chưa đăng nhập
            if (!isUserLoggedIn()) {
                navigateToLogin()
            }
        }

        binding.viewPage.adapter = adapter

        TabLayoutMediator(binding.tabMenu, binding.viewPage) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "File Device"
                    tab.setIcon(R.drawable.ic_file_device)
                }
                1 -> {
                    tab.text = "File Com"
                    tab.setIcon(R.drawable.ic_file_community)
                }
                2 -> {
                    tab.text = "File Share"
                    tab.setIcon(R.drawable.ic_file_share)
                }
            }
        }.attach()
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.viewPage.currentItem != 0) {
            binding.viewPage.currentItem = 0 // Quay lại tab "File Device" (tab 0)
        } else {
            super.onBackPressed() // Nếu đang ở tab đầu tiên, thực hiện hành động quay lại mặc định
        }
    }

    private fun isUserLoggedIn(): Boolean {
        // Kiểm tra trạng thái đăng nhập từ SharedPreferences
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun navigateToLogin() {
        // Kiểm tra xem LoginActivity đã được mở chưa
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
