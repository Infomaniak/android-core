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
package com.infomaniak.core.ksuite.myksuite.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infomaniak.core.ksuite.myksuite.R

internal enum class MyKSuiteUpgradeFeatures(@StringRes val title: Int, @DrawableRes val icon: Int) {

    DriveStorageFeature(
        title = R.string.myKSuiteUpgradeDriveLabel,
        icon = R.drawable.ic_drive_cloud,
    ),

    DriveDropboxFeature(
        title = R.string.myKSuiteUpgradeDropboxLabel,
        icon = R.drawable.ic_folder_circle_filled_arrow_up,
    ),

    MailUnlimitedFeature(
        title = R.string.myKSuiteUpgradeUnlimitedMailLabel,
        icon = R.drawable.ic_paperplane,
    ),

    MailOtherFeature(
        title = R.string.myKSuiteUpgradeRedirectLabel,
        icon = R.drawable.ic_enveloppe_italic,
    ),

    MoreFeatures(title = R.string.myKSuiteUpgradeLabel, icon = R.drawable.ic_gift),
}
