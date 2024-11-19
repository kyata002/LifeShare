package com.example.doan.view.ui.viewfile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doan.R

import android.util.Log
import android.widget.TextView
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class DocxViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docx_view)

        val filePath = intent.getStringExtra("FILE_PATH")
        val textView: TextView = findViewById(R.id.text_view)

        if (filePath != null) {
            val file = File(filePath)
            if (file.exists() && file.extension.equals("docx", ignoreCase = true)) {
                try {
                    val docContent = readDocxFile(file)
                    textView.text = docContent
                } catch (e: IOException) {
                    Log.e("DocxViewActivity", "Error reading DOCX file", e)
                    textView.text = "Không thể đọc tệp DOCX."
                }
            } else {
                textView.text = "Đây không phải tệp DOCX."
            }
        }
    }

    // Đọc và giải nén tệp DOCX
    private fun readDocxFile(file: File): String {
        val zipInputStream = ZipInputStream(FileInputStream(file))
        var entry: ZipEntry?
        val sb = StringBuilder()

        while (zipInputStream.nextEntry.also { entry = it } != null) {
            // Chỉ quan tâm đến tệp document.xml trong DOCX
            if (entry?.name == "word/document.xml") {
                // Đọc nội dung XML từ document.xml
                val content = parseXml(zipInputStream)
                sb.append(content)
            }
            zipInputStream.closeEntry()
        }
        zipInputStream.close()
        return sb.toString()
    }

    // Phân tích nội dung XML từ document.xml
    private fun parseXml(inputStream: ZipInputStream): String {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val sb = StringBuilder()

        // Đọc và phân tích XML
        try {
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.next()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        if (tagName == "t") {
                            sb.append(parser.nextText()) // Lấy văn bản trong tag <t>
                        }
                    }
                    else -> { }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }
}
