package com.example.doan.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.doan.Adapter.MainPagerAdapter
import com.example.doan.R
import com.example.doan.databinding.ActivityMainBinding
import com.example.doan.ui.activity.LoginActivity
import com.example.doan.viewmodel.ViewModelMain
import com.google.android.material.tabs.TabLayoutMediator
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ViewModelMain by viewModels()

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPagerWithTabs()
        binding.viewPage.currentItem = 1

        binding.btnAccount.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.btnAccount)
            popupMenu.menuInflater.inflate(R.menu.file_options_main, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.option_profile -> {
                        navigateToActivity(ProfileActivity::class.java)
                        true
                    }

                    R.id.option_setting -> {
                        Toast.makeText(this, "Cài đặt selected", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.option_logout -> {
                        Toast.makeText(this, "Chi tiết selected", Toast.LENGTH_SHORT).show()
                        true
                    }

                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }
    }

    private fun setupViewPagerWithTabs() {
        val adapter = MainPagerAdapter(this) {
            if (!isUserLoggedIn()) {
                navigateToActivity(LoginActivity::class.java)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.file_options_main, menu)
        return true
    }




    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }

}
