package com.example.doan.viewmodel

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
import com.example.doan.ui.activity.MainActivity
import com.example.doan.ui.activity.SplashActivity
import com.example.doan.ui.dialog.PermissionRequestDialog
import java.io.File

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> get() = _files
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
    fun loadFile(context: Context, type: String): ArrayList<File> {
        val list = ArrayList<File>()
        val table = MediaStore.Files.getContentUri("external")
        val selection = "_data LIKE '%.$type'"

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(table, null, selection, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (cursor == null || cursor.count <= 0 || !cursor.moveToFirst()) {
                // this means error, or simply no results found
                return list
            }
            val data = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            do {
                val path = cursor.getString(data)
                val file = File(path)
                if (file.length() == 0L) {
                    continue
                }
                list.add(file)
            } while (cursor.moveToNext())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }
    fun loadFiles(type: String) {
        // Load files based on the type
        val fileList = loadFile(getApplication(), type)
        _files.postValue(fileList)
    }
}