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

internal enum class ProFeature(@StringRes val title: Int, @StringRes val bold: Int?, @DrawableRes val icon: Int) {

    DriveBusiness(
        title = R.string.kSuiteBusinessKDriveLabel,
        bold = R.string.kSuiteKDriveLabelBold,
        icon = R.drawable.ic_folder_outlined,
    ),

    DriveEnterprise(
        title = R.string.kSuiteEnterpriseKDriveLabel,
        bold = R.string.kSuiteKDriveLabelBold,
        icon = R.drawable.ic_folder_outlined,
    ),

    Euria(
        title = R.string.kSuiteStandardEuriaLabel,
        bold = R.string.kSuiteStandardEuriaLabelBold,
        icon = R.drawable.ic_euria,
    ),

    Functionalities(
        title = R.string.kSuiteEnterpriseFunctionalityLabel,
        bold = R.string.kSuiteEnterpriseFunctionalityLabelBold,
        icon = R.drawable.ic_stairs_arrow,
    ),

    Mail(
        title = R.string.kSuiteStandardMailLabel,
        bold = R.string.kSuiteStandardMailLabelBold,
        icon = R.drawable.ic_mail,
    ),

    Microsoft(
        title = R.string.kSuiteBusinessMicrosoftLabel,
        bold = R.string.kSuiteBusinessMicrosoftLabelBold,
        icon = R.drawable.ic_microsoft,
    ),

    Security(
        title = R.string.kSuiteBusinessSecurityLabel,
        bold = R.string.kSuiteBusinessSecurityLabelBold,
        icon = R.drawable.ic_lock,
    ),

    StorageStandard(
        title = R.string.kSuiteStandardStorageLabel,
        bold = R.string.kSuiteStandardStorageLabelBold,
        icon = R.drawable.ic_cloud_disk,
    ),

    StorageBusiness(
        title = R.string.kSuiteBusinessStorageLabel,
        bold = R.string.kSuiteBusinessStorageLabelBold,
        icon = R.drawable.ic_cloud_disk,
    ),

    StorageEnterprise(
        title = R.string.kSuiteEnterpriseStorageLabel,
        bold = R.string.kSuiteEnterpriseStorageLabelBold,
        icon = R.drawable.ic_cloud_disk,
    ),

    ChatStandard(
        title = R.string.kSuiteStandardKChatLabel,
        bold = R.string.kSuiteKChatLabelBold,
        icon = R.drawable.ic_speech_bubble,
    ),

    ChatBusiness(
        title = R.string.kSuiteBusinessKChatLabel,
        bold = R.string.kSuiteKChatLabelBold,
        icon = R.drawable.ic_speech_bubble,
    ),

    ChatEnterprise(
        title = R.string.kSuiteEnterpriseKChatLabel,
        bold = R.string.kSuiteKChatLabelBold,
        icon = R.drawable.ic_speech_bubble,
    ),

    More(
        title = R.string.kSuiteMoreLabel,
        bold = null,
        icon = R.drawable.ic_plus_circle,
    ),
}
