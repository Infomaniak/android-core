package com.infomaniak.lib.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiToken(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int = 7200,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("scope") val scope: String? = null,
    var expiresAt: Long?
) : Parcelable
