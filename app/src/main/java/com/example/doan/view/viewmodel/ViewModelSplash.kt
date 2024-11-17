package com.example.doan.view.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doan.view.ui.activity.MainActivity
import com.example.doan.view.ui.activity.SplashActivity
import com.example.doan.view.ui.dialog.PermissionRequestDialog
import java.io.File

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> get() = _files

    private val _navigateTo = MutableLiveData<Class<*>>()
    val navigateTo: LiveData<Class<*>> = _navigateTo

    private val countDownTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // You can implement something here to show countdown if needed
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
            Environment.isExternalStorageManager() // For Android 11 and above
        } else {
            SplashActivity.PERMISSIONS_STORAGE.all { permission ->
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    // Load files of specific type
    private fun loadFilesOfType(context: Context, type: String): List<File> {
        val list = mutableListOf<File>()
        val table = MediaStore.Files.getContentUri("external")
        val selection = "_data LIKE '%.$type'"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(table, null, selection, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    do {
                        val filePath = it.getString(dataColumn)
                        val file = File(filePath)
                        if (file.length() > 0) {
                            list.add(file)
                        }
                    } while (it.moveToNext())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return list
    }

    // Method to get files of multiple types
    fun getFileList() {
        val types = listOf("xlsx", "pdf", "mp4","mp3","txt","jpg") // Add more file types as needed
        val allFiles = mutableListOf<File>()
        types.forEach { type ->
            val filesOfType = loadFilesOfType(getApplication<Application>().applicationContext, type)
            allFiles.addAll(filesOfType)
        }
        _files.postValue(allFiles)
    }
}
