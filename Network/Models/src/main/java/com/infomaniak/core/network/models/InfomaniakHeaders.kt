package com.infomaniak.core.network.models

import okhttp3.Headers
import kotlin.reflect.KProperty

class InfomaniakHeaders(headers: Headers) : HeadersBacking(headers) {
    val lastModified: String? by headerName("last-modified")
}

open class HeadersBacking(val remoteHeaders: Headers) {
    fun headerName(headerName: String): HeaderData {
        return object : HeaderData() {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
                return remoteHeaders[headerName]
            }
        }
    }
}

abstract class HeaderData {
    abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): String?
}
