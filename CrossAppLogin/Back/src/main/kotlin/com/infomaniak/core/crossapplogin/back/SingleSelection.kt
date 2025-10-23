/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.crossapplogin.back

fun newSkippedAccountIdsToKeepSingleSelection(
    accounts: List<ExternalAccount>,
    currentlySkippedAccountIds: Set<Long>
): Set<Long> {
    val missingSkippedAccountIdsCount = accounts.size - (1 + currentlySkippedAccountIds.size)
    return when {
        missingSkippedAccountIdsCount > 0 -> {
            val newAccountIdsToSkip = mutableSetOf<Long>()

            for (account in accounts) {
                if (account.id !in currentlySkippedAccountIds) newAccountIdsToSkip += account.id
                if (newAccountIdsToSkip.size == missingSkippedAccountIdsCount) break
            }
            currentlySkippedAccountIds + newAccountIdsToSkip
        }
        missingSkippedAccountIdsCount < 0 -> {
            // Should not happen, but for completeness.
            currentlySkippedAccountIds.drop(-missingSkippedAccountIdsCount).toSet()
        }
        else -> currentlySkippedAccountIds
    }
}
