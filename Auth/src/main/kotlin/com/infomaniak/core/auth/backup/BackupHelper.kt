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
package com.infomaniak.core.auth.backup

import com.infomaniak.core.common.backup.FullBackupAgent

context(_: FullBackupAgent)
inline fun runTheBackup(defaultBackupCalls: () -> Unit) {
    //TODO: If Block Store is supported, do the following (w/ a higher order function?):

    // 1. Store the tokens in Block Store (return if it fails), with `BlockStoreBackup.backupTokens()`
    // 2. Remove the tokens
    // 3. Let the rest of the backup happen
    // 4. Restore the tokens

}
