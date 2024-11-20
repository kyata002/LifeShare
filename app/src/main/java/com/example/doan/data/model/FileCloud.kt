package com.example.doan.data.model

import java.io.Serializable

data class FileCloud(
    var name: String,
    val size: Long,
    val type: String,

    val lastModified: Long,
    val downloadUrl: String,
    val location:String,
    val fileId: Int
) : Serializable

