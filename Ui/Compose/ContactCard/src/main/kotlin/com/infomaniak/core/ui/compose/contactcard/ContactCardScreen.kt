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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLink
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.launch

private val CardCornerRadius = 12.dp

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
    val isEditing = state is ContactCardUiState.Editing
    val isPreview = state is ContactCardUiState.Preview
    var requestSave by remember { mutableStateOf(false) }
    var showActionsBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = when {
            isEditing -> MaterialTheme.colorScheme.surfaceContainer
            isPreview -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.background
        },
        topBar = {
            when {
                isEditing -> EditorTopBar(
                    onCancel = onCancel,
                    onSave = { requestSave = true },
                )
                isPreview -> PreviewTopBar(
                    onClose = onBack,
                    onMore = { showActionsBottomSheet = true },
                )
                else -> TopAppBar(
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
            }
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

    if (isPreview && showActionsBottomSheet) {
        val previewState = state as ContactCardUiState.Preview
        PreviewActionsBottomSheet(
            onDismiss = { showActionsBottomSheet = false },
            onEdit = {
                showActionsBottomSheet = false
                onEdit(previewState.card)
            },
            onDelete = {
                showActionsBottomSheet = false
                onDelete()
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditorTopBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.contactCardTitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        navigationIcon = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.contactCardCancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        actions = {
            TextButton(onClick = onSave) {
                Text(
                    text = stringResource(R.string.contactCardSaveShort),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PreviewTopBar(
    onClose: () -> Unit,
    onMore: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.contactCardTitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.contactCardCloseDescription),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        actions = {
            IconButton(onClick = onMore) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.contactCardMoreActionsDescription),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun OnboardingContent(
    userName: String,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(Margin.Large)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.onboarding_vcard),
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
    user: User,
    card: Card,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin.Medium, vertical = Margin.Medium),
        ) {
            ContactVCardBloc(user = user, card = card)
        }

        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Margin.Medium),
            shape = RoundedCornerShape(CardCornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.contactCardShare),
                modifier = Modifier.padding(vertical = Margin.Mini),
            )
        }
    }
}

@Composable
private fun ContactVCardBloc(user: User, card: Card) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            QrCodeHeader(user = user, card = card)
            Spacer(Modifier.height(Margin.Medium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${card.firstName} ${card.lastName}".trim(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Margin.Mini))
                Text(
                    text = card.email,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(Margin.Medium))

            ContactInfoRows(
                card = card,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Medium),
            )

            val links = card.links.orEmpty().filter { it.url.isNotBlank() && it.type != CardLinkType.Website }
            if (links.isNotEmpty()) {
                Spacer(Modifier.height(Margin.Medium))
                LinksRow(
                    links = links,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin.Medium, vertical = Margin.Small),
                )
            } else {
                Spacer(Modifier.height(Margin.Medium))
            }
        }
    }
}

@Composable
private fun QrCodeHeader(user: User, card: Card) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val qrSize = (maxWidth * 0.62f).coerceAtMost(240.dp)
        val gradientHeight = qrSize * 0.4f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(qrSize),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }

        Surface(
            shape = RoundedCornerShape(CardCornerRadius),
            modifier = Modifier.padding(top = gradientHeight * 0.5f),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
        ) {
            Box(
                modifier = Modifier
                    .size(qrSize)
                    .padding(Margin.Small),
                contentAlignment = Alignment.Center,
            ) {
                val qrPainter = rememberQrCodePainter(data = card.makeVCardString(forQRCode = true))
                Image(
                    painter = qrPainter,
                    contentDescription = stringResource(R.string.contactCardQrCodeDescription),
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(qrSize * 0.24f)
                        .clip(CircleShape),
                ) {
                    Box(
                        modifier = Modifier.padding(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Avatar(
                            avatarType = AvatarType.fromUser(user),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactInfoRows(card: Card, modifier: Modifier = Modifier) {
    val rows = buildList {
        card.company?.takeIf { it.isNotBlank() }?.let { add(stringResource(R.string.contactCardCompany) to it) }
        card.phone.takeIf { it.isNotBlank() }?.let { add(stringResource(R.string.contactCardPhone) to it) }
        card.links.orEmpty().firstOrNull { it.type == CardLinkType.Website }?.let {
            add(stringResource(R.string.contactCardWebsite) to it.url)
        }
    }
    Column(modifier = modifier) {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Margin.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun LinksRow(links: List<CardLink>, modifier: Modifier = Modifier) {
    val grouped = links.groupBy { it.type }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
    ) {
        CardLinkType.entries.forEach { type ->
            val linksOfType = grouped[type] ?: return@forEach
            Icon(
                imageVector = ImageVector.vectorResource(type.iconRes()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            if (type == CardLinkType.Other && linksOfType.size > 1) {
                Text(
                    text = "+${linksOfType.size - 1}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@DrawableRes
private fun CardLinkType.iconRes(): Int = when (this) {
    CardLinkType.LinkedIn -> R.drawable.ic_linkedin
    CardLinkType.Facebook -> R.drawable.ic_facebook
    CardLinkType.Instagram -> R.drawable.ic_instagram
    CardLinkType.X -> R.drawable.ic_x
    CardLinkType.Other, CardLinkType.Website -> R.drawable.ic_link
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PreviewActionsBottomSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    fun dismissThen(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(bottom = Margin.Medium)) {
            BottomSheetAction(
                icon = Icons.Filled.Edit,
                label = stringResource(R.string.contactCardEdit),
                onClick = { dismissThen(onEdit) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Margin.Medium),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            BottomSheetAction(
                icon = Icons.Filled.Delete,
                label = stringResource(R.string.contactCardDelete),
                onClick = { showDeleteConfirmation = true },
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    dismissThen(onDelete)
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
}

@Composable
private fun BottomSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin.Mini),
        contentPadding = PaddingValues(
            horizontal = Margin.Small,
            vertical = Margin.Small,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(Margin.Medium))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun EditorContent(
    editor: ContactCardEditorState,
    requestSave: Boolean,
    onSaveHandled: () -> Unit,
    onSave: () -> Unit,
    onAddAdditionalUrl: () -> Unit,
    onRemoveAdditionalUrl: (String) -> Unit,
    onUpdateDraft: (ContactCardEditorState) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showValidationError by remember { mutableStateOf(false) }
    val isValid =
        editor.firstName.isNotBlank() && editor.lastName.isNotBlank() && editor.email.isNotBlank() && editor.phone.isNotBlank()

    if (requestSave) {
        onSaveHandled()
        if (isValid) onSave() else showValidationError = true
    }

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
            .verticalScroll(rememberScrollState())
            .padding(vertical = Margin.Medium),
        verticalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        SectionCard(title = stringResource(R.string.contactCardGeneralInfoTitle)) {
            EditorField(
                value = editor.firstName,
                placeholder = stringResource(R.string.contactCardFirstName),
                onValueChange = { onUpdateDraft(editor.copy(firstName = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.lastName,
                placeholder = stringResource(R.string.contactCardLastName),
                onValueChange = { onUpdateDraft(editor.copy(lastName = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.email,
                placeholder = stringResource(R.string.contactCardEmail),
                keyboardType = KeyboardType.Email,
                onValueChange = { onUpdateDraft(editor.copy(email = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.phone,
                placeholder = stringResource(R.string.contactCardPhone),
                keyboardType = KeyboardType.Phone,
                onValueChange = { onUpdateDraft(editor.copy(phone = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.company,
                placeholder = stringResource(R.string.contactCardCompany),
                onValueChange = { onUpdateDraft(editor.copy(company = it)) },
            )
        }

        SectionCard(title = stringResource(R.string.contactCardLinksTitle)) {
            EditorField(
                value = editor.linkedIn,
                placeholder = stringResource(R.string.contactCardLinkedIn),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(linkedIn = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.facebook,
                placeholder = stringResource(R.string.contactCardFacebook),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(facebook = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.instagram,
                placeholder = stringResource(R.string.contactCardInstagram),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(instagram = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.x,
                placeholder = stringResource(R.string.contactCardX),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(x = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.website,
                placeholder = stringResource(R.string.contactCardWebsite),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(website = it)) },
            )

            editor.additionalUrls.forEach { additionalUrl ->
                FieldDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditorField(
                        modifier = Modifier.weight(1f),
                        value = additionalUrl.value,
                        placeholder = stringResource(R.string.contactCardOtherUrl),
                        keyboardType = KeyboardType.Uri,
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
                    IconButton(
                        onClick = { onRemoveAdditionalUrl(additionalUrl.id) },
                        modifier = Modifier.padding(end = Margin.Small),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.contactCardRemoveUrlDescription),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            FieldDivider()
            TextButton(
                onClick = onAddAdditionalUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Mini, vertical = Margin.Mini),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Margin.Mini))
                    Text(
                        text = stringResource(R.string.contactCardAddUrlAction),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = Margin.Small),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = Margin.Medium + Margin.Mini,
                end = Margin.Medium,
                bottom = Margin.Mini,
            ),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(CardCornerRadius),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun FieldDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = Margin.Medium),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun EditorField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
    )
}


//region Previews

private fun previewUser(): User {
    val orgPreference = OrganizationPreference(currentOrganizationId = 0)
    val preferences = Preferences(
        security = null,
        organizationPreference = orgPreference,
    )
    return User(
        id = 42,
        displayName = "Alice Doe",
        firstname = "Alice",
        lastname = "Doe",
        email = "alice.doe@example.com",
        avatar = "https://example.com/avatar.png",
        card = null,
        login = "alice.doe",
        isStaff = false,
        preferences = preferences,
    )
}

private fun previewCard(): Card = Card(
    firstName = "Alice",
    lastName = "Doe",
    email = "alice.doe@example.com",
    phone = "+41 79 123 45 67",
    company = "Infomaniak",
    avatarUrl = "https://example.com/avatar.png",
    links = listOf(
        CardLink(CardLinkType.LinkedIn, "https://linkedin.com/in/alicedoe"),
        CardLink(CardLinkType.Website, "https://example.com"),
        CardLink(CardLinkType.Other, "https://blog.example.com"),
    ),
)

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
