/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.ui.compose.contactcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.infomaniak.core.auth.models.user.Card
import kotlinx.coroutines.launch

class ContactCardFragment : Fragment() {

    private val viewModel: ContactCardViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val uiState = viewModel.uiState.collectAsState(initial = ContactCardUiState.Loading).value
                    ContactCardScreen(
                        state = uiState,
                        onBack = { findNavController().popBackStack() },
                        onCreate = viewModel::startCreate,
                        onEdit = viewModel::startEdit,
                        onDelete = viewModel::deleteCard,
                        onCancel = viewModel::cancelEditing,
                        onSave = viewModel::saveDraft,
                        onAddAdditionalUrl = viewModel::addAdditionalUrl,
                        onRemoveAdditionalUrl = viewModel::removeAdditionalUrl,
                        onUpdateDraft = viewModel::updateDraft,
                        onShare = ::shareCard,
                    )
                }
            }
        }
    }

    private fun shareCard(card: Card) {
        lifecycleScope.launch {
            requireContext().shareContactCard(card)
        }
    }
}
