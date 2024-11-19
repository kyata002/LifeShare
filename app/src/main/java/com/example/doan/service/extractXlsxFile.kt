package com.example.doan.service

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun extractXlsxFile(context: Context, filePath: String): List<File> {
    val files = mutableListOf<File>()
    try {
        val zipInputStream = ZipInputStream(FileInputStream(filePath))
        var zipEntry: ZipEntry? = zipInputStream.nextEntry
        while (zipEntry != null) {
            val extractedFile = File(context.cacheDir, zipEntry.name)
            extractedFile.parentFile?.mkdirs()
            extractedFile.outputStream().use { outputStream ->
                zipInputStream.copyTo(outputStream)
            }
            files.add(extractedFile)
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    } catch (e: Exception) {
        Log.e("ExcelViewActivity", "Error extracting XLSX file", e)
    }
    return files
}
