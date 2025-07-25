package com.infomaniak.core.network.models

import java.net.URLEncoder

/**
 *  CustomHeader : Represents a custom HTTP header allows defining how the header value should be handled.
 */
sealed interface CustomHeader {
    val key: String
    val value: String

    data class AutoEncodeHeaderToUTF8(override val key: String, private val rawValue: String) : CustomHeader {
        override val value: String = URLEncoder.encode(rawValue, "UTF-8")
    }

    data class RawHeader(override val key: String, override val value: String) : CustomHeader
}
