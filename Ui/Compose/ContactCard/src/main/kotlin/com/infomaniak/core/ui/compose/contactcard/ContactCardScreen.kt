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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.ui.compose.contactcard.component.ContactPreviewProvider
import com.infomaniak.core.ui.compose.contactcard.component.DefaultDeleteConfirmationDialog
import com.infomaniak.core.ui.compose.contactcard.component.DefaultTopBar
import com.infomaniak.core.ui.compose.contactcard.component.EditorContent
import com.infomaniak.core.ui.compose.contactcard.component.OnboardingContent
import com.infomaniak.core.ui.compose.contactcard.component.PreviewActionsBottomSheet
import com.infomaniak.core.ui.compose.contactcard.component.PreviewContactData
import com.infomaniak.core.ui.compose.contactcard.component.PreviewContent
import com.infomaniak.core.ui.compose.contactcard.model.ContactCardCallbacks
import com.infomaniak.core.ui.compose.contactcard.model.isValid

@Composable
fun ContactCardScreen(
    onBack: () -> Unit,
    onShare: (Card) -> Unit,
    viewModel: ContactCardViewModel = viewModel(),
    confirmDelete: ((onConfirmed: () -> Unit) -> Unit)? = null,
    confirmValidationError: ((onConfirmed: () -> Unit) -> Unit)? = null,
    topBar: (@Composable (ContactCardTopBarState) -> Unit)? = null,
    colors: ContactCardColors = ContactCardDefaults.colors(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContactCardScreen(
        state = state,
        callbacks = ContactCardCallbacks(
            onBack = onBack,
            onCreate = viewModel::startCreate,
            onEdit = viewModel::startEdit,
            onDelete = viewModel::deleteCard,
            onCancel = viewModel::cancelEditing,
            onSave = viewModel::saveDraft,
            onAddAdditionalUrl = viewModel::addAdditionalUrl,
            onRemoveAdditionalUrl = viewModel::removeAdditionalUrl,
            onUpdateDraft = viewModel::updateDraft,
            onShare = onShare,
            confirmDelete = confirmDelete,
            confirmValidationError = confirmValidationError,
        ),
        topBar = topBar,
        colors = colors,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactCardScreen(
    state: ContactCardUiState,
    callbacks: ContactCardCallbacks,
    topBar: (@Composable (ContactCardTopBarState) -> Unit)? = null,
    colors: ContactCardColors = ContactCardDefaults.colors(),
) {
    TrackScreenEffect(state)

    var requestSave by remember { mutableStateOf(false) }
    var showActionsBottomSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }

    val isPreview = state is ContactCardUiState.Preview
    val isOnboarding = state is ContactCardUiState.Onboarding
    val scaffoldContainerColor = if (isPreview) colors.background else MaterialTheme.colorScheme.background

    LaunchedEffect(requestSave) {
        if (requestSave && state is ContactCardUiState.Editing && state.editor.isValid()) {
            callbacks.onSave(state.user, state.editor, state.existingCard)
        }
    }

    Scaffold(
        containerColor = scaffoldContainerColor,
        topBar = {
            val topBarState = rememberTopBarState(
                state = state,
                onBack = callbacks.onBack,
                onCancel = { callbacks.onCancel(it) },
                onRequestSave = { requestSave = true },
                onShowActions = { showActionsBottomSheet = true },
            )
            topBar?.invoke(topBarState) ?: DefaultTopBar(topBarState)
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
                ContactCardBodyContent(
                    state = state,
                    requestSave = requestSave,
                    callbacks = callbacks,
                    onSaveHandled = { requestSave = false },
                )
            }
        }
    }

    if (isPreview && showActionsBottomSheet) {
        PreviewActionsBottomSheet(
            onDismiss = { showActionsBottomSheet = false },
            onEdit = {
                showActionsBottomSheet = false
                callbacks.onEdit(state.user, state.card)
            },
            onDelete = {
                showActionsBottomSheet = false
                if (callbacks.confirmDelete != null) {
                    callbacks.confirmDelete { callbacks.onDelete(state.user) }
                } else {
                    pendingDelete = true
                }
            },
        )
    }

    if (pendingDelete) {
        val previewState = state as ContactCardUiState.Preview
        DefaultDeleteConfirmationDialog(
            onDismiss = { pendingDelete = false },
            onConfirm = {
                pendingDelete = false
                callbacks.onDelete(previewState.user)
            },
        )
    }
}

@Composable
private fun TrackScreenEffect(state: ContactCardUiState) {
    val isOnboarding = state is ContactCardUiState.Onboarding
    val isEditing = state is ContactCardUiState.Editing
    val isPreview = state is ContactCardUiState.Preview

    LaunchedEffect(isOnboarding, isEditing, isPreview) {
        when {
            isOnboarding -> ContactCardMatomo.trackScreen("ContactCardOnboardingView")
            isEditing -> ContactCardMatomo.trackScreen("ContactCardFromView")
            isPreview -> ContactCardMatomo.trackScreen("ContactCardQRCodeView")
        }
    }
}

private fun rememberTopBarState(
    state: ContactCardUiState,
    onBack: () -> Unit,
    onCancel: (User) -> Unit,
    onRequestSave: () -> Unit,
    onShowActions: () -> Unit,
): ContactCardTopBarState {
    return when (state) {
        is ContactCardUiState.Editing -> ContactCardTopBarState.Editor(
            onCancel = { onCancel(state.user) },
            onSave = onRequestSave,
        )
        is ContactCardUiState.Preview -> ContactCardTopBarState.Preview(
            onClose = onBack,
            onMore = onShowActions,
        )
        is ContactCardUiState.Onboarding -> ContactCardTopBarState.Onboarding(onBack = onBack)
        else -> ContactCardTopBarState.Default(onBack = onBack)
    }
}

@Composable
private fun ContactCardBodyContent(
    state: ContactCardUiState,
    requestSave: Boolean,
    callbacks: ContactCardCallbacks,
    onSaveHandled: () -> Unit,
) {
    when (state) {
        ContactCardUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        ContactCardUiState.Closed -> {
            LaunchedEffect(Unit) {
                callbacks.onBack()
            }
        }
        is ContactCardUiState.Onboarding -> OnboardingContent(
            modifier = Modifier.fillMaxSize(),
            onCreate = { callbacks.onCreate(state.user) },
        )
        is ContactCardUiState.Preview -> PreviewContent(
            modifier = Modifier.fillMaxSize(),
            user = state.user,
            card = state.card,
            onShare = { callbacks.onShare(state.card) },
        )
        is ContactCardUiState.Editing -> EditorContent(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            editor = state.editor,
            requestSave = requestSave,
            onSaveHandled = onSaveHandled,
            callbacks = callbacks,
        )
    }
}

//region Previews

@Preview(name = "Loading")
@Composable
private fun ContactCardScreenLoadingPreview() {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Loading,
                callbacks = ContactCardCallbacks(
                    onBack = {},
                    onCreate = { _ -> },
                    onEdit = { _, _ -> },
                    onDelete = { _ -> },
                    onCancel = { _ -> },
                    onSave = { _, _, _ -> },
                    onAddAdditionalUrl = {},
                    onRemoveAdditionalUrl = {},
                    onUpdateDraft = {},
                    onShare = {},
                ),
            )
        }
    }
}

@Preview(name = "Onboarding")
@Composable
private fun ContactCardScreenOnboardingPreview(
    @PreviewParameter(ContactPreviewProvider::class) contactData: PreviewContactData,
) {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Onboarding(user = contactData.user),
                callbacks = ContactCardCallbacks(
                    onBack = {},
                    onCreate = { _ -> },
                    onEdit = { _, _ -> },
                    onDelete = { _ -> },
                    onCancel = { _ -> },
                    onSave = { _, _, _ -> },
                    onAddAdditionalUrl = {},
                    onRemoveAdditionalUrl = {},
                    onUpdateDraft = {},
                    onShare = {},
                ),
            )
        }
    }
}

@Preview(name = "Preview")
@Composable
private fun ContactCardScreenPreviewPreview(
    @PreviewParameter(ContactPreviewProvider::class) contactData: PreviewContactData,
) {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Preview(
                    user = contactData.user.copy(card = contactData.card),
                    card = contactData.card,
                ),
                callbacks = ContactCardCallbacks(
                    onBack = {},
                    onCreate = { _ -> },
                    onEdit = { _, _ -> },
                    onDelete = { _ -> },
                    onCancel = { _ -> },
                    onSave = { _, _, _ -> },
                    onAddAdditionalUrl = {},
                    onRemoveAdditionalUrl = {},
                    onUpdateDraft = {},
                    onShare = {},
                ),
            )
        }
    }
}

@Preview(name = "Editing")
@Composable
private fun ContactCardScreenEditingPreview(
    @PreviewParameter(ContactPreviewProvider::class) contactData: PreviewContactData,
) {
    MaterialTheme {
        Surface {
            ContactCardScreen(
                state = ContactCardUiState.Editing(
                    user = contactData.user,
                    editor = ContactCardEditorState.fromCard(contactData.card, fallbackAvatarUrl = contactData.user.avatar),
                    existingCard = contactData.card,
                ),
                callbacks = ContactCardCallbacks(
                    onBack = {},
                    onCreate = { _ -> },
                    onEdit = { _, _ -> },
                    onDelete = { _ -> },
                    onCancel = { _ -> },
                    onSave = { _, _, _ -> },
                    onAddAdditionalUrl = {},
                    onRemoveAdditionalUrl = {},
                    onUpdateDraft = {},
                    onShare = {},
                ),
            )
        }
    }
}

//endregion
