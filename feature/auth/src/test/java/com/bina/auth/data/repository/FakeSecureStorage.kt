package com.bina.auth.data.repository

import com.bina.security.storage.SecureStorage

class FakeSecureStorage : SecureStorage {
    private val store = mutableMapOf<String, String>()

    override fun save(key: String, value: String) { store[key] = value }
    override fun get(key: String): String? = store[key]
    override fun remove(key: String) { store.remove(key) }
    override fun clear() { store.clear() }

    fun isEmpty(): Boolean = store.isEmpty()
}
