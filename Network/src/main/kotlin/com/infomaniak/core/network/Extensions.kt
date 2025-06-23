/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.network

import com.google.gson.JsonSyntaxException
import kotlinx.serialization.SerializationException

fun Exception.isNetworkException(): Boolean {
    val okHttpException = arrayOf("stream closed", "required settings preface not received")
    return this.javaClass.name.contains("java.net.", ignoreCase = true) ||
            this.javaClass.name.contains("javax.net.", ignoreCase = true) ||
            this is java.io.InterruptedIOException ||
            this is okhttp3.internal.http2.StreamResetException ||
            (this is java.io.IOException && this.message?.lowercase() in okHttpException)
}

fun Exception.isSerializationException(): Boolean {
    return this is JsonSyntaxException || this is SerializationException || this is IllegalArgumentException
}
