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
package com.infomaniak.core.crossloginui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.pluralStringResource
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.screens.CrossLoginAccountsBottomSheet
import com.infomaniak.core.crossloginui.screens.components.CrossLoginItem
import com.infomaniak.core.crossloginui.theme.CrossLoginXMLTheme

@OptIn(ExperimentalMaterial3Api::class)
class CrossLoginAccountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val accounts = mutableStateListOf<CrossLoginUiAccount>()

    private var onAccountClickListener: ((account: CrossLoginUiAccount) -> Unit)? = null
    private var onAnotherAccountClickListener: (() -> Unit)? = null

    @Composable
    override fun Content() {
        CrossLoginXMLTheme {

            var shouldDisplayBottomSheet by rememberSaveable { mutableStateOf(false) }

            val selectedAccounts = accounts.filter { it.isSelected }
            val count = selectedAccounts.count()
            when {
                count == 1 -> {
                    val account = selectedAccounts.first()
                    CrossLoginItem(
                        title = account.name,
                        description = account.email,
                        iconUrl = account.avatarUrl,
                        isSelected = { true },
                        onClick = { shouldDisplayBottomSheet = true },
                    )
                }
                count > 1 -> {
                    CrossLoginItem(
                        title = pluralStringResource(R.plurals.selectedAccountCountLabel, count, count),
                        iconsUrls = selectedAccounts.mapNotNull { it.avatarUrl },
                        isSelected = { true },
                        onClick = { shouldDisplayBottomSheet = true },
                    )
                }
            }

            if (shouldDisplayBottomSheet) {
                CrossLoginAccountsBottomSheet(
                    accounts = { accounts },
                    onAccountClicked = { onAccountClickListener?.invoke(it) },
                    onAnotherAccountClicked = { onAnotherAccountClickListener?.invoke() },
                    onDismissRequest = { shouldDisplayBottomSheet = false },
                )
            }
        }
    }

    fun setOnAccountClickListener(listener: (CrossLoginUiAccount) -> Unit) {
        onAccountClickListener = listener
    }

    fun setOnAnotherAccountClickListener(listener: () -> Unit) {
        onAnotherAccountClickListener = listener
    }

    fun setAccounts(list: List<CrossLoginUiAccount>) {
        accounts.apply {
            clear()
            addAll(list)
        }
    }
}
