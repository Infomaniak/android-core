/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either (at your option) any later version.
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.ui.compose.contactcard.component.ContactCardTopBar
import com.infomaniak.core.ui.compose.contactcard.component.DefaultDeleteConfirmationDialog
import com.infomaniak.core.ui.compose.contactcard.component.EditorContent
import com.infomaniak.core.ui.compose.contactcard.component.EditorTopBar
import com.infomaniak.core.ui.compose.contactcard.component.LoadingContent
import com.infomaniak.core.ui.compose.contactcard.component.OnboardingContent
import com.infomaniak.core.ui.compose.contactcard.component.PreviewActionsBottomSheet
import com.infomaniak.core.ui.compose.contactcard.component.PreviewContent
import com.infomaniak.core.ui.compose.contactcard.component.PreviewTopBar
import com.infomaniak.core.ui.compose.contactcard.component.previewCard
import com.infomaniak.core.ui.compose.contactcard.component.previewUser

@Composable
fun ContactCardScreen(
    onBack: () -> Unit,
    onShare: (Card) -> Unit,
    viewModel: ContactCardViewModel = viewModel(),
    confirmDelete: ((onConfirmed: () -> Unit) -> Unit)? = null,
    topBar: (@Composable (ContactCardTopBarState) -> Unit)? = null,
    colors: ContactCardColors = ContactCardDefaults.colors(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf(false) }

    val handleDelete: () -> Unit = if (confirmDelete != null) {
        { confirmDelete { viewModel.deleteCard() } }
    } else {
        { pendingDelete = true }
    }

    ContactCardScreen(
        state = state,
        onBack = onBack,
        onCreate = viewModel::startCreate,
        onEdit = viewModel::startEdit,
        onDelete = handleDelete,
        onCancel = viewModel::cancelEditing,
        onSave = viewModel::saveDraft,
        onAddAdditionalUrl = viewModel::addAdditionalUrl,
        onRemoveAdditionalUrl = viewModel::removeAdditionalUrl,
        onUpdateDraft = viewModel::updateDraft,
        onShare = onShare,
        topBar = topBar,
        colors = colors,
    )

    if (pendingDelete) {
        DefaultDeleteConfirmationDialog(
            onDismiss = { pendingDelete = false },
            onConfirm = {
                pendingDelete = false
                viewModel.deleteCard()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactCardScreen(
    state: ContactCardUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (Card) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onAddAdditionalUrl: () -> Unit,
    onRemoveAdditionalUrl: (String) -> Unit,
    onUpdateDraft: (ContactCardEditorState) -> Unit,
    onShare: (Card) -> Unit,
    topBar: (@Composable (ContactCardTopBarState) -> Unit)? = null,
    colors: ContactCardColors = ContactCardDefaults.colors(),
) {
    val isEditing = state is ContactCardUiState.Editing
    val isPreview = state is ContactCardUiState.Preview
    val isOnboarding = state is ContactCardUiState.Onboarding
    var requestSave by remember { mutableStateOf(false) }
    var showActionsBottomSheet by remember { mutableStateOf(false) }

    val scaffoldContainerColor = when {
        isPreview -> colors.background
        else -> MaterialTheme.colorScheme.background
    }

    Scaffold(
        containerColor = scaffoldContainerColor,
        topBar = {
            val topBarState: ContactCardTopBarState = when {
                isEditing -> ContactCardTopBarState.Editor(
                    onCancel = onCancel,
                    onSave = { requestSave = true },
                )
                isPreview -> ContactCardTopBarState.Preview(
                    onClose = onBack,
                    onMore = { showActionsBottomSheet = true },
                )
                else -> ContactCardTopBarState.Default(onBack = onBack)
            }
            if (topBar != null) topBar(topBarState) else DefaultTopBar(topBarState)
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isOnboarding) {
                Image(
                    painter = painterResource(R.drawable.ic_back_wave),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.FillWidth,
                    colorFilter = ColorFilter.tint(colors.waveBackground),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                when (state) {
                    ContactCardUiState.Loading -> LoadingContent()
                    is ContactCardUiState.Onboarding -> OnboardingContent(
                        modifier = Modifier.fillMaxSize(),
                        userName = "${state.user.firstname} ${state.user.lastname}",
                        onCreate = onCreate,
                    )
                    is ContactCardUiState.Preview -> PreviewContent(
                        modifier = Modifier.fillMaxSize(),
                        user = state.user,
                        card = state.card,
                        onShare = { onShare(state.card) },
                    )
                    is ContactCardUiState.Editing -> EditorContent(
                        modifier = Modifier.fillMaxSize(),
                        editor = state.editor,
                        requestSave = requestSave,
                        onSaveHandled = { requestSave = false },
                        onSave = onSave,
                        onAddAdditionalUrl = onAddAdditionalUrl,
                        onRemoveAdditionalUrl = onRemoveAdditionalUrl,
                        onUpdateDraft = onUpdateDraft,
                    )
                }
            }
        }
    }

    if (isPreview && showActionsBottomSheet) {
        PreviewActionsBottomSheet(
            onDismiss = { showActionsBottomSheet = false },
            onEdit = {
                showActionsBottomSheet = false
                onEdit(state.card)
            },
            onDelete = {
                showActionsBottomSheet = false
                onDelete()
            },
        )
    }
}

//region Previews

@Composable
private fun DefaultTopBar(state: ContactCardTopBarState) {
    when (state) {
        is ContactCardTopBarState.Editor -> EditorTopBar(
            onCancel = state.onCancel,
            onSave = state.onSave,
        )
        is ContactCardTopBarState.Preview -> PreviewTopBar(
            onClose = state.onClose,
            onMore = state.onMore,
        )
        is ContactCardTopBarState.Default -> ContactCardTopBar(
            navigationIcon = {
                IconButton(onClick = state.onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource((R.drawable.ic_cross)),
                        contentDescription = stringResource(R.string.contentDescriptionBackButton),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun ContactCardScreenLoadingPreview() {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Loading,
                onBack = {},
                onCreate = {},
                onEdit = {},
                onDelete = {},
                onCancel = {},
                onSave = {},
                onAddAdditionalUrl = {},
                onRemoveAdditionalUrl = {},
                onUpdateDraft = {},
                onShare = {},
            )
        }
    }
}

@Preview(name = "Onboarding")
@Composable
private fun ContactCardScreenOnboardingPreview() {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Onboarding(user = previewUser()),
                onBack = {},
                onCreate = {},
                onEdit = {},
                onDelete = {},
                onCancel = {},
                onSave = {},
                onAddAdditionalUrl = {},
                onRemoveAdditionalUrl = {},
                onUpdateDraft = {},
                onShare = {},
            )
        }
    }
}

@Preview(name = "Preview")
@Composable
private fun ContactCardScreenPreviewPreview() {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Preview(
                    user = previewUser().copy(card = previewCard()),
                    card = previewCard(),
                ),
                onBack = {},
                onCreate = {},
                onEdit = {},
                onDelete = {},
                onCancel = {},
                onSave = {},
                onAddAdditionalUrl = {},
                onRemoveAdditionalUrl = {},
                onUpdateDraft = {},
                onShare = {},
            )
        }
    }
}

@Preview(name = "Editing")
@Composable
private fun ContactCardScreenEditingPreview() {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Editing(
                    user = previewUser(),
                    editor = ContactCardEditorState.fromCard(previewCard(), fallbackAvatarUrl = "https://example.com/avatar.png"),
                    existingCard = previewCard(),
                ),
                onBack = {},
                onCreate = {},
                onEdit = {},
                onDelete = {},
                onCancel = {},
                onSave = {},
                onAddAdditionalUrl = {},
                onRemoveAdditionalUrl = {},
                onUpdateDraft = {},
                onShare = {},
            )
        }
    }
}

//endregion
