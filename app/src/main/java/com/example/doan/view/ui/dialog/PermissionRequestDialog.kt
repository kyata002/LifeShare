package com.example.doan.view.ui.dialog

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.doan.databinding.DialogPermissionrequestBinding
import com.example.doan.view.ui.activity.MainActivity

class PermissionRequestDialog : AppCompatActivity() {
    private lateinit var binding: DialogPermissionrequestBinding

    private val PERMISSIONS_STORAGE = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val RQC_REQUEST_PERMISSION_ANDROID_BELOW = 1
    private val RQC_REQUEST_PERMISSION_ANDROID_11 = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogPermissionrequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        event()
    }

    private fun event() {
        binding.btAllow.setOnClickListener {
            if (checkPermission()) {
                navigateToActivity(MainActivity::class.java)
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED
        } else {
            Environment.isExternalStorageManager()
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        finish()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                RQC_REQUEST_PERMISSION_ANDROID_BELOW
            )
        } else {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, RQC_REQUEST_PERMISSION_ANDROID_11)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, RQC_REQUEST_PERMISSION_ANDROID_11)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQC_REQUEST_PERMISSION_ANDROID_11) {
            if (Environment.isExternalStorageManager()) {
                navigateToActivity(MainActivity::class.java)
            } else {
                // Permission not granted, handle as needed
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RQC_REQUEST_PERMISSION_ANDROID_BELOW) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                navigateToActivity(MainActivity::class.java)
            } else {
                // Permission denied, handle as needed
            }
        }
    }
}
