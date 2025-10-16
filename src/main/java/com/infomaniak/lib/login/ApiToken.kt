package com.infomaniak.lib.login

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ApiToken(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int = 7200,
    @SerialName("user_id") val userId: Int,
    @SerialName("scope") val scope: String? = null,
    @Transient var expiresAt: Long? = null
) : Parcelable
