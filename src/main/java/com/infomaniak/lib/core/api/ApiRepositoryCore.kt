package com.infomaniak.lib.core.api

import com.infomaniak.lib.core.models.ApiResponse
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.utils.ApiController
import okhttp3.OkHttpClient

abstract class ApiRepositoryCore {

    fun getUserProfile(
        okHttpClient: OkHttpClient,
        withEmails: Boolean = false,
        withPhones: Boolean = false,
        withSecurity: Boolean = false
    ): ApiResponse<User> {
        var with = ""
        if (withEmails) with += "emails"
        if (withPhones) with += "phones"
        if (withSecurity) with += "security"
        if (with.isNotEmpty()) with = "?with=$with"

        val url = "${ApiRoutesCore.getUserProfile()}$with"
        return ApiController.callApi(url, ApiController.ApiMethod.GET, okHttpClient = okHttpClient)
    }

}