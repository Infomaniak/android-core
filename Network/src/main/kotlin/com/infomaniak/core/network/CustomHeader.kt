package com.infomaniak.core.network

class CustomHeader {
    private val customHeaders: MutableMap<String, String> = mutableMapOf()

    fun get(): List<Pair<String, String>> {
        return customHeaders.toList()
    }

    fun add(key: String, value: String) {
        customHeaders[key] = value
    }

    fun clear() {
        customHeaders.clear()
    }
}
