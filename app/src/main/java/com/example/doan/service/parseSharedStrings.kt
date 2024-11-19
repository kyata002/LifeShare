package com.example.doan.service

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream

fun parseSharedStrings(sharedStringsFile: File): List<String> {
    val sharedStrings = mutableListOf<String>()
    try {
        val inputStream = FileInputStream(sharedStringsFile)
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var currentText = ""
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        currentText = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    currentText = parser.text
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        sharedStrings.add(currentText)
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return sharedStrings
}
