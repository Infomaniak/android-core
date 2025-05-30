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
import androidx.navigation.fragment.navArgs
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteDashboardScreen
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteDashboardScreenData
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteXMLTheme

open class MyKSuiteDashboardFragment : Fragment() {

    private val navigationArgs: MyKSuiteDashboardFragmentArgs by navArgs()

    private var composeView: ComposeView? = null

    private val onClose: () -> Unit by lazy { { this@MyKSuiteDashboardFragment.findNavController().popBackStack() } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            composeView = this
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            resetContent(navigationArgs.dashboardData)
        }
    }

    protected fun resetContent(dashboardData: MyKSuiteDashboardScreenData) {
        composeView?.setContent {
            MyKSuiteXMLTheme { MyKSuiteDashboardScreen(dashboardScreenData = { dashboardData }, onClose = onClose) }
        }
    }
}
