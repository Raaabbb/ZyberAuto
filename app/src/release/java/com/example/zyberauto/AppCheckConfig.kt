package com.example.zyberauto

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckConfig {
    fun getProviderFactory(): AppCheckProviderFactory {
        return PlayIntegrityAppCheckProviderFactory.getInstance()
    }
}
