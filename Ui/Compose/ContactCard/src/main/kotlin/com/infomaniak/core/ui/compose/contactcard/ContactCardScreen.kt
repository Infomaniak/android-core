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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ContactCardScreen(
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
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.contactCardTitle)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues ->
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
                    onEdit = { onEdit(state.card) },
                    onDelete = onDelete,
                    onShare = { onShare(state.card) },
                )
                is ContactCardUiState.Editing -> EditorContent(
                    modifier = Modifier.fillMaxSize(),
                    editor = state.editor,
                    onCancel = onCancel,
                    onSave = onSave,
                    onAddAdditionalUrl = onAddAdditionalUrl,
                    onRemoveAdditionalUrl = onRemoveAdditionalUrl,
                    onUpdateDraft = onUpdateDraft,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun OnboardingContent(
    modifier: Modifier,
    userName: String,
    onCreate: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(Margin.Large)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.illustration_onboarding),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
        Spacer(Modifier.height(Margin.Medium))
        Text(text = stringResource(R.string.contactCardOnboardingTitle), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Margin.Small))
        Text(text = stringResource(R.string.contactCardOnboardingDescription, userName))
        Spacer(Modifier.height(Margin.Large))
        Button(onClick = onCreate) {
            Text(text = stringResource(R.string.contactCardCreate))
        }
    }
}

@Composable
private fun PreviewContent(
    modifier: Modifier,
    user: User,
    card: Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete()
                }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.contactCardDelete)) },
            text = { Text(text = stringResource(R.string.contactCardDeleteConfirmation)) },
        )
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Margin.Medium),
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(Margin.Medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Margin.Medium),
            ) {
                Avatar(
                    avatarType = AvatarType.fromUser(user),
                    modifier = Modifier.padding(top = Margin.Small),
                )

                Text(
                    text = "${card.firstName} ${card.lastName}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(text = card.email, color = MaterialTheme.colorScheme.primary)

                ContactCardQrCode(card)

                ContactCardInfoRows(card = card)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
            verticalArrangement = Arrangement.spacedBy(Margin.Small),
        ) {
            Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.contactCardShare))
            }
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.contactCardEdit))
            }
            Button(onClick = { showDeleteConfirmation = true }, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.contactCardDelete))
            }
        }
    }
}

@Composable
private fun EditorContent(
    modifier: Modifier,
    editor: ContactCardEditorState,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onAddAdditionalUrl: () -> Unit,
    onRemoveAdditionalUrl: (String) -> Unit,
    onUpdateDraft: (ContactCardEditorState) -> Unit,
) {
    var showValidationError by remember { mutableStateOf(false) }
    val isValid =
        editor.firstName.isNotBlank() && editor.lastName.isNotBlank() && editor.email.isNotBlank() && editor.phone.isNotBlank()

    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            confirmButton = {
                TextButton(onClick = { showValidationError = false }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            title = { Text(text = stringResource(R.string.contactCardValidationTitle)) },
            text = { Text(text = stringResource(R.string.contactCardValidationDescription)) },
        )
    }

    Column(
        modifier = modifier
            .padding(Margin.Medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Margin.Small),
    ) {
        ContactCardTextField(
            value = editor.firstName,
            label = stringResource(R.string.contactCardFirstName),
            onValueChange = { onUpdateDraft(editor.copy(firstName = it)) },
        )
        ContactCardTextField(
            value = editor.lastName,
            label = stringResource(R.string.contactCardLastName),
            onValueChange = { onUpdateDraft(editor.copy(lastName = it)) },
        )
        ContactCardTextField(
            value = editor.email,
            label = stringResource(R.string.contactCardEmail),
            keyboardType = KeyboardType.Email,
            onValueChange = { onUpdateDraft(editor.copy(email = it)) },
        )
        ContactCardTextField(
            value = editor.phone,
            label = stringResource(R.string.contactCardPhone),
            keyboardType = KeyboardType.Phone,
            onValueChange = { onUpdateDraft(editor.copy(phone = it)) },
        )
        ContactCardTextField(
            value = editor.company,
            label = stringResource(R.string.contactCardCompany),
            onValueChange = { onUpdateDraft(editor.copy(company = it)) },
        )

        Text(text = stringResource(R.string.contactCardLinksTitle), style = MaterialTheme.typography.titleMedium)

        ContactCardTextField(
            value = editor.linkedIn,
            label = stringResource(R.string.contactCardLinkedIn),
            keyboardType = KeyboardType.Uri,
            leadingIcon = true,
            onValueChange = { onUpdateDraft(editor.copy(linkedIn = it)) },
        )
        ContactCardTextField(
            value = editor.facebook,
            label = stringResource(R.string.contactCardFacebook),
            keyboardType = KeyboardType.Uri,
            leadingIcon = true,
            onValueChange = { onUpdateDraft(editor.copy(facebook = it)) },
        )
        ContactCardTextField(
            value = editor.instagram,
            label = stringResource(R.string.contactCardInstagram),
            keyboardType = KeyboardType.Uri,
            leadingIcon = true,
            onValueChange = { onUpdateDraft(editor.copy(instagram = it)) },
        )
        ContactCardTextField(
            value = editor.x,
            label = stringResource(R.string.contactCardX),
            keyboardType = KeyboardType.Uri,
            leadingIcon = true,
            onValueChange = { onUpdateDraft(editor.copy(x = it)) },
        )
        ContactCardTextField(
            value = editor.website,
            label = stringResource(R.string.contactCardWebsite),
            keyboardType = KeyboardType.Uri,
            leadingIcon = true,
            onValueChange = { onUpdateDraft(editor.copy(website = it)) },
        )

        editor.additionalUrls.forEach { additionalUrl ->
            Row(horizontalArrangement = Arrangement.spacedBy(Margin.Small), verticalAlignment = Alignment.CenterVertically) {
                ContactCardTextField(
                    value = additionalUrl.value,
                    label = stringResource(R.string.contactCardOtherUrl),
                    keyboardType = KeyboardType.Uri,
                    leadingIcon = true,
                    modifier = Modifier.weight(1f),
                    onValueChange = { value ->
                        onUpdateDraft(
                            editor.copy(
                                additionalUrls = editor.additionalUrls.map {
                                    if (it.id == additionalUrl.id) it.copy(value = value) else it
                                }
                            )
                        )
                    },
                )
                TextButton(onClick = { onRemoveAdditionalUrl(additionalUrl.id) }) {
                    Text(text = stringResource(R.string.contactCardRemoveUrl))
                }
            }
        }

        OutlinedButton(onClick = onAddAdditionalUrl) { Text(text = stringResource(R.string.contactCardAddUrl)) }

        Spacer(Modifier.height(Margin.Small))
        Button(
            onClick = { if (isValid) onSave() else showValidationError = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.contactCardSave))
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(android.R.string.cancel))
        }
    }
}

@Composable
private fun ContactCardTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        leadingIcon = if (leadingIcon) {
            {
                Icon(imageVector = Icons.Filled.AttachFile, contentDescription = null)
            }
        } else {
            null
        },
    )
}

@Composable
private fun ContactCardQrCode(card: Card) {
    val painter = rememberQrCodePainter(data = card.makeVCardString(forQRCode = true))
    Image(
        painter = painter,
        contentDescription = stringResource(R.string.contactCardQrCodeDescription),
        modifier = Modifier
            .padding(vertical = Margin.Small)
            .fillMaxWidth()
            .height(240.dp),
    )
}

@Composable
private fun ContactCardInfoRows(card: Card) {
    val rows = buildList {
        card.company?.takeIf { it.isNotBlank() }?.let { add(stringResource(R.string.contactCardCompany) to it) }
        card.phone.takeIf { it.isNotBlank() }?.let { add(stringResource(R.string.contactCardPhone) to it) }
        card.links.orEmpty().firstOrNull { it.type == CardLinkType.Website }?.let {
            add(stringResource(R.string.contactCardWebsite) to it.url)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Margin.Small), modifier = Modifier.fillMaxWidth()) {
        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = label, color = MaterialTheme.colorScheme.primary)
                Text(text = value, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
