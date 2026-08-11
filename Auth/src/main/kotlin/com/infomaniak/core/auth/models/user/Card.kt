/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.auth.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Card(
    @SerializedName("first_name") @SerialName("first_name")
    val firstName: String,
    @SerializedName("last_name") @SerialName("last_name")
    val lastName: String,
    val email: String,
    val phone: String,
    val company: String? = null,
    @SerializedName("avatar_url") @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val links: List<CardLink>? = null,
) : Parcelable {

    fun makeVCardString(forQRCode: Boolean = false, avatarBase64: String? = null, avatarMimeType: String? = null): String {
        return buildString {
            appendVCardLine("BEGIN:VCARD")
            appendVCardLine("VERSION:3.0")
            appendVCardLine("N:${lastName.escapeVCardValue()};${firstName.escapeVCardValue()};;;")
            appendVCardLine("FN:${listOf(firstName, lastName).filter(String::isNotBlank).joinToString(" ").escapeVCardValue()}")

            company?.takeIf(String::isNotBlank)?.let {
                appendVCardLine("ORG:${it.escapeVCardValue()}")
            }

            if (phone.isNotBlank()) appendVCardLine("TEL;TYPE=CELL:${phone.escapeVCardValue()}")
            if (email.isNotBlank()) appendVCardLine("EMAIL;TYPE=INTERNET:${email.escapeVCardValue()}")

            if (forQRCode) {
                avatarUrl?.let { appendVCardLine("PHOTO;VALUE=URL;TYPE=${avatarMimeType ?: "JPEG"}:$it") }
            } else {
                avatarBase64?.let { appendVCardLine("PHOTO;ENCODING=b;TYPE=${avatarMimeType ?: "JPEG"}:$it") }
            }

            links.orEmpty().forEach { link ->
                appendVCardLine("URL:${link.url.escapeVCardValue()}")
            }

            appendVCardLine("END:VCARD")
        }
    }

    private fun StringBuilder.appendVCardLine(content: String) {
        append(content).append("\r\n")
    }
}

@Serializable
@Parcelize
data class CardLink(
    val type: CardLinkType,
    val url: String,
) : Parcelable

@Serializable
enum class CardLinkType {
    @SerialName("linkedIn")
    @SerializedName("linkedIn")
    LinkedIn,
    @SerialName("x")
    @SerializedName("x")
    X,
    @SerialName("instagram")
    @SerializedName("instagram")
    Instagram,
    @SerialName("facebook")
    @SerializedName("facebook")
    Facebook,
    @SerialName("website")
    @SerializedName("website")
    Website,
    @SerialName("other")
    @SerializedName("other")
    Other,
}

private fun String.escapeVCardValue(): String {
    return buildString(length) {
        for (character in this@escapeVCardValue) {
            when (character) {
                '\\' -> append("\\\\")
                ';' -> append("\\;")
                ',' -> append("\\,")
                '\n' -> append("\\n")
                '\r' -> Unit
                else -> append(character)
            }
        }
    }
}
