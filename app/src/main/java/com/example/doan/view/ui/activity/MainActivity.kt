package com.example.doan.view.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.doan.Adapter.MainPagerAdapter
import com.example.doan.R
import com.example.doan.const.Companion.ACTION_SORT_FILES
import com.example.doan.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        setupStatusBar()
        setupViewPagerWithTabs()
        saveSortOption("clear")

//        binding.main.setOnClickListener {
//            navigateToActivity()
//        }

        binding.btnAccount.setOnClickListener {
            if(!isUserLoggedIn()){
                navigateToActivity(LoginActivity::class.java)
            }else{

                val popupMenu = PopupMenu(this, binding.btnAccount)
                popupMenu.menuInflater.inflate(R.menu.popup_options_main, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.option_profile -> {
                            navigateToActivity(ProfileActivity::class.java)
                            true
                        }

                        R.id.option_setting -> {
                            navigateToActivity(SettingActivity::class.java)
                            true
                        }

                        R.id.option_logout -> {
                            Toast.makeText(this, "Đăng xuất...", Toast.LENGTH_SHORT).show()
                            auth.signOut() // Log out the user
                            navigateToActivity(LoginActivity::class.java) // Redirect to login
                            true
                        }

                        else -> false
                    }
                }
                popupMenu.show()
            }
        }




    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.Main)
        }
    }

    private fun setupViewPagerWithTabs() {
        val adapter = MainPagerAdapter(this) {
            if (!isUserLoggedIn()) {
                navigateToActivity(LoginActivity::class.java)
                false // Return false if not logged in
            } else {
                true // User is logged in
            }
        }

        binding.viewPage.adapter = adapter

        TabLayoutMediator(binding.tabMenu, binding.viewPage) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Thiết Bị"
                }
                1 -> {
                    tab.text = "Đám Mây"
                    tab.setIcon(R.drawable.ic_file_community)
                }
                2 -> {
                    tab.text = "Chia Sẻ"
                    tab.setIcon(R.drawable.ic_file_share)
                }
            }

            // Set a click listener for each tab
            tab.view.setOnClickListener {
                // Execute any specific action for each tab here
                when (position) {
                    0 -> {
                        // No login check for "File Device" tab
                    }
                    1 -> {
                        // Check login for "File Com" tab
                        if (!isUserLoggedIn()) {
                            navigateToActivity(LoginActivity::class.java)
                        }
                    }
                    2 -> {
                        // Check login for "File Share" tab
                        if (!isUserLoggedIn()) {
                            navigateToActivity(LoginActivity::class.java)
                        }
                    }
                }
            }
        }.attach()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.popup_options_main, menu)
        return true
    }

    private fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        if(targetActivity==LoginActivity::class.java){
            val intent = Intent(this, targetActivity)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, targetActivity)
            startActivity(intent)
        }
    }
    private fun saveSortOption(sortOption: String) {
        val editor = sharedPreferences.edit()
        editor.putString("sort_option", sortOption)
        editor.apply()
    }


}
