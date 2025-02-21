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
package com.infomaniak.core.myksuite.ui.utils

import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import com.infomaniak.core.myksuite.ui.screens.KSuiteApp
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteDashboardScreenData
import com.infomaniak.core.myksuite.ui.views.MyKSuiteDashboardFragment
import com.infomaniak.core.myksuite.ui.views.MyKSuiteUpgradeBottomSheetDialog

object MyKSuiteUiUtils {

    internal const val DEEPLINK_BASE = "android-app://com.infomaniak.core.myksuite"

    fun NavController.openMyKSuiteUpgradeBottomSheet(app: KSuiteApp) {
        NavDeepLinkRequest.Builder
            .fromUri(MyKSuiteUpgradeBottomSheetDialog.getDeeplink(app).toUri())
            .build()
            .also(::navigate)
    }

    fun NavController.openMyKSuiteDashboard(data: MyKSuiteDashboardScreenData) {
        NavDeepLinkRequest.Builder
            .fromUri(MyKSuiteDashboardFragment.getDeeplink(data).toUri())
            .build()
            .also(::navigate)
    }
}
