package com.example.zyberauto.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    @get:com.google.firebase.firestore.PropertyName("isVerified")
    @set:com.google.firebase.firestore.PropertyName("isVerified")
    var isVerified: Boolean = false
)
