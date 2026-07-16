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

sealed interface ContactCardTopBarState {
    data class Editor(val onCancel: () -> Unit, val onSave: () -> Unit) : ContactCardTopBarState
    data class Preview(val onClose: () -> Unit, val onMore: () -> Unit) : ContactCardTopBarState
    data class Default(val onBack: () -> Unit) : ContactCardTopBarState
}
