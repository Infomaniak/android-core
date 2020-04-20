package com.infomaniak.lib.login

import com.google.gson.annotations.SerializedName

class ApiToken(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("scope") val scope: String? = null
)