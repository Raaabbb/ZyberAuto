package com.example.zyberauto.presentation.auth

sealed class LoginOtpStatus {
    object Idle : LoginOtpStatus()
    object Sending : LoginOtpStatus()
    object CodeSent : LoginOtpStatus()
    object Verifying : LoginOtpStatus()
    object Verified : LoginOtpStatus()
    data class Error(val msg: String) : LoginOtpStatus()
}
