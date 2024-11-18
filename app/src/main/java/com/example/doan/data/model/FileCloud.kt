package com.example.doan.data.model

import java.io.Serializable

data class FileCloud(
    val name: String,
    val size: Long,
    val type: String,
    val path: String,
    val lastModified: Long,
    val downloadUrl: String,
    val fileId: Int
) : Serializable

