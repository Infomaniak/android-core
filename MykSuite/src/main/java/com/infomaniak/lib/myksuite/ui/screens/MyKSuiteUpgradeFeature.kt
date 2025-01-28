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
package com.infomaniak.lib.myksuite.ui.screens

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infomaniak.lib.myksuite.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyKSuiteUpgradeFeatures(@StringRes val title: Int, @DrawableRes val icon: Int) : Parcelable {

    internal companion object {
        val MoreFeatures = MyKSuiteUpgradeFeatures(title = R.string.myKSuiteUpgradeLabel, icon = R.drawable.ic_gift)
    }
}
