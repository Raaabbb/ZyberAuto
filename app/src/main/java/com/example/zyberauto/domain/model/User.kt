package com.example.zyberauto.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    var isVerified: Boolean = false,
    val password: String = "" // Added for local auth
)
