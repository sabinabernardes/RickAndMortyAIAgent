package com.bina.security.di

import com.bina.security.storage.EncryptedPrefsStorage
import com.bina.security.storage.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val securityModule = module {
    single<SecureStorage> { EncryptedPrefsStorage(androidContext()) }
}
