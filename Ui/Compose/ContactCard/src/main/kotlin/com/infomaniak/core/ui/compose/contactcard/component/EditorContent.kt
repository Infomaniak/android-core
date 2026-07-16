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
package com.infomaniak.core.ui.compose.contactcard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.ui.compose.contactcard.ContactCardEditorState
import com.infomaniak.core.ui.compose.contactcard.R
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
internal fun EditorContent(
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
            title = { Text(text = stringResource(R.string.alertTitle)) },
            text = { Text(text = stringResource(R.string.alertDescription)) },
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Margin.Medium),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Margin.Medium),
    ) {
        SectionCard(title = stringResource(R.string.generalInformation)) {
            EditorField(
                value = editor.firstName,
                placeholder = "${stringResource(R.string.firstName)}*",
                onValueChange = { onUpdateDraft(editor.copy(firstName = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.lastName,
                placeholder = "${stringResource(R.string.lastName)}*",
                onValueChange = { onUpdateDraft(editor.copy(lastName = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.email,
                placeholder = "${stringResource(R.string.email)}*",
                keyboardType = KeyboardType.Email,
                onValueChange = { onUpdateDraft(editor.copy(email = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.phone,
                placeholder = "${stringResource(R.string.phone)}*",
                keyboardType = KeyboardType.Phone,
                onValueChange = { onUpdateDraft(editor.copy(phone = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.company,
                placeholder = stringResource(R.string.company),
                onValueChange = { onUpdateDraft(editor.copy(company = it)) },
            )
        }

        SectionCard(title = stringResource(R.string.linksAndSocialNetwork)) {
            EditorField(
                value = editor.linkedIn,
                placeholder = stringResource(R.string.linkedIn),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(linkedIn = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.facebook,
                placeholder = stringResource(R.string.facebook),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(facebook = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.instagram,
                placeholder = stringResource(R.string.instagram),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(instagram = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.x,
                placeholder = stringResource(R.string.x),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(x = it)) },
            )
            FieldDivider()
            EditorField(
                value = editor.website,
                placeholder = stringResource(R.string.webSite),
                keyboardType = KeyboardType.Uri,
                onValueChange = { onUpdateDraft(editor.copy(website = it)) },
            )

            editor.additionalUrls.forEach { additionalUrl ->
                FieldDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditorField(
                        modifier = Modifier.weight(1f),
                        value = additionalUrl.value,
                        placeholder = "${stringResource(R.string.otherUrl)}*",
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
                            imageVector = ImageVector.vectorResource(R.drawable.ic_bin),
                            contentDescription = stringResource(R.string.deleteButton),
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
                        text = stringResource(R.string.addUrl),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = Margin.Small),
                    )
                }
            }
        }
    }
}

@Preview(name = "EditorContent")
@Composable
private fun EditorContentPreview() {
    MaterialTheme {
        Surface {
            EditorContent(
                editor = ContactCardEditorState.fromCard(previewCard(), fallbackAvatarUrl = "https://example.com/avatar.png"),
                requestSave = false,
                onSaveHandled = {},
                onSave = {},
                onAddAdditionalUrl = {},
                onRemoveAdditionalUrl = {},
                onUpdateDraft = {},
            )
        }
    }
}
