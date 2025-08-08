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
package com.infomaniak.core.ksuite.ksuitepro.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infomaniak.core.ksuite.ksuitepro.R

internal enum class ProFeature(@StringRes val title: Int, @DrawableRes val icon: Int) {

    StandardStorage(
        title = R.string.kSuiteStandardStorageLabel,
        icon = R.drawable.ic_ksuite_storage,
    ),

    BusinessStorage(
        title = R.string.kSuiteBusinessStorageLabel,
        icon = R.drawable.ic_ksuite_storage,
    ),

    EnterpriseStorage(
        title = R.string.kSuiteEnterpriseStorageLabel,
        icon = R.drawable.ic_ksuite_storage,
    ),

    StandardChat(
        title = R.string.kSuiteStandardKChatLabel,
        icon = R.drawable.ic_ksuite_chat,
    ),

    BusinessChat(
        title = R.string.kSuiteBusinessKChatLabel,
        icon = R.drawable.ic_ksuite_chat,
    ),

    EnterpriseChat(
        title = R.string.kSuiteEnterpriseKChatLabel,
        icon = R.drawable.ic_ksuite_chat,
    ),

    StandardMail(
        title = R.string.kSuiteStandardMailLabel,
        icon = R.drawable.ic_ksuite_mail,
    ),

    BusinessDrive(
        title = R.string.kSuiteBusinessKDriveLabel,
        icon = R.drawable.ic_ksuite_drive,
    ),

    EnterpriseFunctionality(
        title = R.string.kSuiteEnterpriseFunctionalityLabel,
        icon = R.drawable.ic_ksuite_functionality,
    ),

    StandardEuria(
        title = R.string.kSuiteStandardEuriaLabel,
        icon = R.drawable.ic_ksuite_euria,
    ),

    BusinessSecurity(
        title = R.string.kSuiteBusinessSecurityLabel,
        icon = R.drawable.ic_ksuite_security,
    ),

    EnterpriseMicrosoft(
        title = R.string.kSuiteEnterpriseMicrosoftLabel,
        icon = R.drawable.ic_ksuite_microsoft,
    ),

    More(
        title = R.string.kSuiteMoreLabel,
        icon = R.drawable.ic_ksuite_more,
    ),
}
