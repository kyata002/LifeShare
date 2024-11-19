package com.example.doan.view.viewmodel

import FileApp
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
import com.example.doan.data.model.FileVideo
import com.example.doan.view.ui.activity.MainActivity
import com.example.doan.view.ui.activity.SplashActivity
import com.example.doan.view.ui.dialog.PermissionRequestDialog
import java.io.File

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _files = MutableLiveData<List<FileApp>>()
    val files: LiveData<List<FileApp>> get() = _files
    private var videpList = mutableListOf<FileVideo>()

    private val _mp4Files = MutableLiveData<List<FileApp>>()
    val mp4Files: LiveData<List<FileApp>> get() = _mp4Files

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

    fun getFileList() {
        val types = listOf("xlsx", "pdf", "mp4", "mp3", "txt", "jpg", "png", "docx") // Add more file types as needed
        val allFiles = mutableListOf<FileApp>()

        types.forEach { type ->
            val filesOfType = loadFilesOfType(getApplication<Application>().applicationContext, type)
            val fileApps = filesOfType.map { file ->
                FileApp(
                    name = file.name,
                    size = file.length(),
                    type = type,
                    path = file.absolutePath,
                    lastModified = file.lastModified()
                )
            }
            allFiles.addAll(fileApps)
        }

        // Post all files to LiveData
        _files.postValue(allFiles)

        // Filter and post only mp4 files
        val mp4Files = allFiles.filter { it.type == "mp4" }
        _mp4Files.postValue(mp4Files)

        // Manually update videpList with the mp4 files
        videpList = mp4Files.map { file ->
            FileVideo(
                name = file.name,
                path = file.path,
                size = file.size,
                type = file.type,
                lastModified = file.lastModified
            )
        }.toMutableList()
    }
    data class FileVideo(
        val name: String,
        val path: String,
        val size: Long,
        val type: String,
        val lastModified: Long
    )
}
