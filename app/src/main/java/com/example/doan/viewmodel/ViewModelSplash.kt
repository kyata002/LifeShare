package com.example.doan.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doan.ui.activity.MainActivity
import com.example.doan.ui.activity.SplashActivity
import com.example.doan.ui.dialog.PermissionRequestDialog

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val _navigateTo = MutableLiveData<Class<*>>()
    val navigateTo: LiveData<Class<*>> = _navigateTo

    private val countDownTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            if (checkPermission()) {
                _navigateTo.value = MainActivity::class.java
            } else {
                _navigateTo.value = PermissionRequestDialog::class.java
            }
        }
    }

    fun startCountDown() {
        countDownTimer.start()
    }

    private fun checkPermission(): Boolean {
        val context = getApplication<Application>().applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            SplashActivity.PERMISSIONS_STORAGE.all { permission ->
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}