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
package com.infomaniak.core.myksuite.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteDashboardScreen
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteDashboardScreenData
import com.infomaniak.core.myksuite.ui.utils.MyKSuiteUiUtils.DEEPLINK_BASE
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class MyKSuiteDashboardFragment : Fragment() {

    private val dashboardData: MyKSuiteDashboardScreenData? by lazy {
        arguments?.getString(DASHBOARD_DATA_KEY)?.let { jsonData ->
            runCatching { Json.decodeFromString(jsonData) as MyKSuiteDashboardScreenData }.getOrNull()
        }
    }

    private var composeView: ComposeView? = null

    private val onClose: () -> Unit by lazy { { this@MyKSuiteDashboardFragment.findNavController().popBackStack() } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (dashboardData == null) findNavController().popBackStack()

        return ComposeView(requireContext()).apply {
            composeView = this
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            dashboardData?.let { data -> setContent { MyKSuiteDashboardScreen({ data }, onClose) } }
        }
    }

    protected fun resetContent(dashboardData: MyKSuiteDashboardScreenData) {
        composeView?.setContent { MyKSuiteDashboardScreen(dashboardScreenData = { dashboardData }, onClose = onClose) }
    }

    companion object {
        // Must kept the same value as the deepLink's in `my_ksuite_navigation`
        private const val DASHBOARD_DATA_KEY = "dashboardData"

        internal fun getDeeplink(dashboardData: MyKSuiteDashboardScreenData): String {
            return "$DEEPLINK_BASE/myKSuiteDashboardFragment/${Json.encodeToString(dashboardData)}"
        }
    }
}
