/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
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
package com.infomaniak.core.legacy.bugtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.appversionchecker.data.api.ApiRepositoryAppVersion
import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.legacy.networking.HttpClient
import com.infomaniak.core.legacy.networking.HttpUtils
import com.infomaniak.core.legacy.networking.ManualAuthorizationRequired
import com.infomaniak.core.legacy.utils.SingleLiveEvent
import com.infomaniak.core.legacy.utils.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BugTrackerViewModel : ViewModel() {
    val files = mutableListOf<BugTrackerActivity.BugTrackerFile>()
    val bugReportResult = SingleLiveEvent<Boolean>()

    fun sendBugReport(formBuilder: MultipartBody.Builder) = viewModelScope.launch(Dispatchers.IO) {
        files.forEachIndexed { index, file ->
            formBuilder.addFormDataPart(
                "file_$index",
                file.fileName,
                file.bytes.toRequestBody(file.mimeType?.toMediaTypeOrNull())
            )
        }

        bugReportResult.postValue(apiSendBugReport(formBuilder.build()))
    }

    private suspend fun apiSendBugReport(multipartBody: MultipartBody): Boolean {
        var isSuccessful = false
        runCatching {
            @OptIn(ManualAuthorizationRequired::class)
            val request = Request.Builder()
                .url(REPORT_URL)
                .headers(HttpUtils.getHeaders())
                .post(multipartBody)
                .build()

            isSuccessful = HttpClient.okHttpClientLongTimeout.newBuilder().build().newCall(request).await().isSuccessful
        }.onFailure { exception ->
            if (exception is CancellationException) throw exception
            exception.printStackTrace()
        }

        return isSuccessful
    }

    fun checkIfUpdateIsAvailable(
        appId: String,
        appVersion: String,
        store: AppVersion.Store,
        channelFilter: AppVersion.VersionChannel,
    ): Flow<Boolean> = flow {
        val projectionFields = listOf(
            AppVersion.ProjectionFields.MinVersion,
            AppVersion.ProjectionFields.PublishedVersionsTag,
            AppVersion.ProjectionFields.PublishedVersionType
        )

        val apiResponse = ApiRepositoryAppVersion.getAppVersion(
            appName = appId,
            store = store,
            projectionFields = projectionFields,
            channelFilter = channelFilter,
            okHttpClient = HttpClient.okHttpClientNoTokenInterceptor
        )

        emit(apiResponse.data?.updateIsAvailable(appVersion, channelFilter) ?: false)
    }

    private companion object {
        const val REPORT_URL = "https://welcome.infomaniak.com/api/web-components/1/report"
    }
}
