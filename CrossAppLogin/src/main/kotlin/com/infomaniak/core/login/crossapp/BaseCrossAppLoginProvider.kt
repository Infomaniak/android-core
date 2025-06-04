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
@file:OptIn(ExperimentalSerializationApi::class)

package com.infomaniak.core.login.crossapp

import android.app.Service
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Process
import com.infomaniak.core.login.crossapp.internal.SingleBlobCursor
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateCheckerImpl
import com.infomaniak.core.login.crossapp.internal.certificates.infomaniakAppsCertificates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * **NOTE**: This [ContentProvider] based approach might be used only for prototyping,
 * and possibly not for the final version, where the bound [Service] based approach
 * in [BaseCrossAppLoginService] might be favored for resource usage reasons.
 */
abstract class BaseCrossAppLoginProvider : ContentProvider() {

    private val scope = CoroutineScope(Dispatchers.Default)

    @ExperimentalSerializationApi
    protected abstract val accountsFlow: Flow<List<ExternalAccount>>

    private val certificateChecker: AppCertificateChecker = AppCertificateCheckerImpl(
        signingCertificates = infomaniakAppsCertificates
    )

    private val accountsSharedFlow = accountsFlow.map { accounts ->
        val byteArray = ProtoBuf.encodeToByteArray(accounts)
        SingleBlobCursor(byteArray)
    }.shareIn(
        scope = scope,
        started = SharingStarted.Lazily,
        replay = 1
    )

    final override fun onCreate(): Boolean = true

    final override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? {
        val callingUid = Binder.getCallingUid()
        check(callingUid != Process.myUid())
        return runBlocking {
            val allowed = certificateChecker.isUidAllowed(callingUid)
            if (allowed) accountsSharedFlow.first() else throw SecurityException("The calling app wasn't recognized")
        }
    }

    final override fun getType(uri: Uri): String? = null

    final override fun insert(uri: Uri, values: ContentValues?) = unsupported()

    final override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?
    ) = unsupported()

    final override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?
    ) = unsupported()

    private fun unsupported(): Nothing = throw UnsupportedOperationException()
}
