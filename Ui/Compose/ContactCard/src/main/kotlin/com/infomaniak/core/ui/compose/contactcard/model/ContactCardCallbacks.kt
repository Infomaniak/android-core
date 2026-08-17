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
package com.infomaniak.core.ui.compose.contactcard.model

import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.ui.compose.contactcard.ContactCardEditorState

data class ContactCardCallbacks(
    val onBack: () -> Unit,
    val onCreate: (User) -> Unit,
    val onEdit: (User, Card) -> Unit,
    val onDelete: (User) -> Unit,
    val onCancel: (User) -> Unit,
    val onSave: (User, ContactCardEditorState, Card?) -> Unit,
    val onAddAdditionalUrl: () -> Unit,
    val onRemoveAdditionalUrl: (String) -> Unit,
    val onUpdateDraft: (ContactCardEditorState) -> Unit,
    val onShare: (Card) -> Unit,
    val confirmDelete: ((onConfirmed: () -> Unit) -> Unit)? = null,
    val confirmValidationError: (() -> Unit)? = null,
)

fun ContactCardEditorState.isValid(): Boolean =
    firstName.trim().isNotEmpty() &&
            lastName.trim().isNotEmpty() &&
            email.trim().isNotEmpty() &&
            phone.trim().isNotEmpty()
