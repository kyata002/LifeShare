package com.example.doan.data.model

data class AccountModel(
    val id: String,      // Change to String to store Firebase user UID
    val username: String,
    val email: String,
    val password: String,
    val numberphone: String,
    val listAppUp:List<String>,
    val listShare:List<String>
)
