/*
 * Infomaniak kMail - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.core.githubTools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.infomaniak.lib.core.api.ApiController
import com.infomaniak.lib.core.networking.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import okhttp3.Request

class GitHubViewModel : ViewModel() {

    fun getLastRelease(repo: String) = liveData(Dispatchers.IO) {
        val request = Request.Builder().url("${API_GITHUB_URL}${repo}/releases").get().build()
        val response = HttpClient.okHttpClientNoInterceptor.newBuilder().build().newCall(request).execute()
        val bodyResponse = response.body?.string() ?: ""
        val lastRelease = if (response.isSuccessful && bodyResponse.isNotBlank()) {
            val releases = ApiController.json.decodeFromString<List<GithubRelease>>(bodyResponse)
            releases.find { !it.draft && !it.prerelease }
        } else {
            null
        }
        emit(lastRelease)
    }

    private companion object {
        const val API_GITHUB_URL = "https://api.github.com/repos/Infomaniak/"
    }
}