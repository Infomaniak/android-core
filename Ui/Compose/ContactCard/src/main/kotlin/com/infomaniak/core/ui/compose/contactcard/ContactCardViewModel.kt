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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.auth.UserAccountUtils
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLink
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ContactCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val accountUtils = UserAccountUtils(application.applicationContext)
    private val userId: Int = requireNotNull(savedStateHandle.get<Int>(USER_ID_KEY)) { "userId argument is required" }

    private val _uiState = MutableStateFlow<ContactCardUiState>(ContactCardUiState.Loading)
    val uiState: StateFlow<ContactCardUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            val user = accountUtils.getUserById(userId)
            currentUser = user
            if (_uiState.value !is ContactCardUiState.Editing) {
                _uiState.value = user?.toUiState() ?: ContactCardUiState.Error
            }
        }
    }

    fun startCreate() {
        val user = currentUser ?: return
        _uiState.value = ContactCardUiState.Editing(
            user = user,
            editor = ContactCardEditorState.fromUser(user),
            existingCard = null,
        )
    }

    fun startEdit(card: Card) {
        val user = currentUser ?: return
        _uiState.value = ContactCardUiState.Editing(
            user = user,
            editor = ContactCardEditorState.fromCard(card, user.avatar),
            existingCard = card,
        )
    }

    fun cancelEditing() {
        _uiState.value = currentUser?.toUiState() ?: ContactCardUiState.Error
    }

    fun updateDraft(editor: ContactCardEditorState) {
        val current = _uiState.value as? ContactCardUiState.Editing ?: return
        _uiState.value = current.copy(editor = editor)
    }

    fun addAdditionalUrl() {
        val current = _uiState.value as? ContactCardUiState.Editing ?: return
        updateDraft(current.editor.copy(additionalUrls = current.editor.additionalUrls + EditableUrl()))
    }

    fun removeAdditionalUrl(id: String) {
        val current = _uiState.value as? ContactCardUiState.Editing ?: return
        updateDraft(current.editor.copy(additionalUrls = current.editor.additionalUrls.filterNot { it.id == id }))
    }

    fun saveDraft() {
        val current = _uiState.value as? ContactCardUiState.Editing ?: return

        viewModelScope.launch {
            val card = current.editor.toCard(current.user.avatar)
            val updatedUser = current.user.copy(card = card)
            accountUtils.updateUser(updatedUser)
            currentUser = updatedUser
            _uiState.value = ContactCardUiState.Preview(user = updatedUser, card = card)
        }
    }

    fun deleteCard() {
        val current = _uiState.value as? ContactCardUiState.Preview ?: return

        viewModelScope.launch {
            val updatedUser = current.user.copy(card = null)
            accountUtils.updateUser(updatedUser)
            currentUser = updatedUser
            _uiState.value = ContactCardUiState.Onboarding(updatedUser)
        }
    }

    private fun User.toUiState(): ContactCardUiState {
        return card?.let { ContactCardUiState.Preview(user = this, card = it) } ?: ContactCardUiState.Onboarding(this)
    }

    companion object {
        const val USER_ID_KEY = "userId"
    }
}

sealed interface ContactCardUiState {
    data object Loading : ContactCardUiState
    data object Error : ContactCardUiState
    data class Onboarding(val user: User) : ContactCardUiState
    data class Preview(val user: User, val card: Card) : ContactCardUiState
    data class Editing(val user: User, val editor: ContactCardEditorState, val existingCard: Card?) : ContactCardUiState
}

data class ContactCardEditorState(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val company: String,
    val avatarUrl: String?,
    val linkedIn: String,
    val x: String,
    val instagram: String,
    val facebook: String,
    val website: String,
    val additionalUrls: List<EditableUrl>,
) {
    fun toCard(fallbackAvatarUrl: String?): Card {
        val links = buildList {
            website.trim().takeIf(String::isNotEmpty)?.let { add(CardLink(CardLinkType.Website, it)) }
            linkedIn.trim().takeIf(String::isNotEmpty)?.let { add(CardLink(CardLinkType.LinkedIn, it)) }
            facebook.trim().takeIf(String::isNotEmpty)?.let { add(CardLink(CardLinkType.Facebook, it)) }
            instagram.trim().takeIf(String::isNotEmpty)?.let { add(CardLink(CardLinkType.Instagram, it)) }
            x.trim().takeIf(String::isNotEmpty)?.let { add(CardLink(CardLinkType.X, it)) }
            additionalUrls.mapNotNull { it.value.trim().takeIf(String::isNotEmpty) }.forEach {
                add(CardLink(CardLinkType.Other, it))
            }
        }.takeIf { it.isNotEmpty() }

        return Card(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            company = company.trim().takeIf(String::isNotBlank),
            avatarUrl = avatarUrl?.takeIf(String::isNotBlank) ?: fallbackAvatarUrl,
            links = links,
        )
    }

    companion object {
        fun fromUser(user: User): ContactCardEditorState {
            return ContactCardEditorState(
                firstName = user.firstname,
                lastName = user.lastname,
                email = user.email,
                phone = "",
                company = "",
                avatarUrl = user.avatar,
                linkedIn = "",
                x = "",
                instagram = "",
                facebook = "",
                website = "",
                additionalUrls = emptyList(),
            )
        }

        fun fromCard(card: Card, fallbackAvatarUrl: String?): ContactCardEditorState {
            val websiteLinks = card.links.orEmpty().filter { it.type == CardLinkType.Website }
            val otherUrls = buildList {
                websiteLinks.drop(1).forEach { add(it.url) }
                card.links.orEmpty().filter { it.type == CardLinkType.Other }.forEach { add(it.url) }
            }

            return ContactCardEditorState(
                firstName = card.firstName,
                lastName = card.lastName,
                email = card.email,
                phone = card.phone,
                company = card.company.orEmpty(),
                avatarUrl = card.avatarUrl ?: fallbackAvatarUrl,
                linkedIn = card.links.orEmpty().firstOrNull { it.type == CardLinkType.LinkedIn }?.url.orEmpty(),
                x = card.links.orEmpty().firstOrNull { it.type == CardLinkType.X }?.url.orEmpty(),
                instagram = card.links.orEmpty().firstOrNull { it.type == CardLinkType.Instagram }?.url.orEmpty(),
                facebook = card.links.orEmpty().firstOrNull { it.type == CardLinkType.Facebook }?.url.orEmpty(),
                website = websiteLinks.firstOrNull()?.url.orEmpty(),
                additionalUrls = otherUrls.map { EditableUrl(value = it) },
            )
        }
    }
}

data class EditableUrl(
    val id: String = UUID.randomUUID().toString(),
    val value: String = "",
)
