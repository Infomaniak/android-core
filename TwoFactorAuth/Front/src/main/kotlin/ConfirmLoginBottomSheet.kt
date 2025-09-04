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
@file:OptIn(ExperimentalUuidApi::class)

package com.infomaniak.core.twofactorauth.front

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.infomaniak.core.compose.basics.rememberCallableState
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth
import com.infomaniak.core.twofactorauth.back.TwoFactorAuthImpl
import com.infomaniak.core.twofactorauth.back.TwoFactorAuthViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmLoginAutoManagedBottomSheet() {
    val confirmRequest = rememberCallableState<Boolean>()
    val sheetState = rememberModalBottomSheetState()
    val twoFactorAuth: TwoFactorAuthViewModel = TODO()
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
    ) {
        ConfirmLoginBottomSheetContent(
            attemptTimeMark = TODO(),
            connectionAttemptInfo = TODO(),
            confirmRequest = confirmRequest
        )
    }
}
