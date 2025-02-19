/*
 * Infomaniak Mail - Android
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.infomaniak.core.extensions.capitalizeFirstChar
import com.infomaniak.core.myksuite.ui.screens.KSuiteApp
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteUpgradeBottomSheet
import com.infomaniak.core.utils.enumValueOfOrNull

@OptIn(ExperimentalMaterial3Api::class)
class MyKSuiteUpgradeBottomSheetDialog : BottomSheetDialogFragment() {

    private val kSuiteApp by lazy {
        arguments?.getString(K_SUITE_APP_KEY)?.let { app -> enumValueOfOrNull<KSuiteApp>(app.lowercase().capitalizeFirstChar()) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (kSuiteApp == null) findNavController().popBackStack()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            kSuiteApp?.let {
                setContent {
                    MyKSuiteUpgradeBottomSheet(
                        onDismissRequest = this@MyKSuiteUpgradeBottomSheetDialog.findNavController()::popBackStack,
                        app = it,
                    )
                }
            }
        }
    }

    companion object {
        private const val K_SUITE_APP_KEY = "kSuiteApp" // Must kept the same value as the deepLink's in `my_ksuite_navigation`
    }
}
