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
package com.infomaniak.core.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job

suspend inline fun <R> waitForComplete(block: (completable: CompletableJob) -> R): R {
    val completable: CompletableJob = Job()
    return block(completable).also { completable.join() }
}

suspend inline fun <R> waitForValue(block: (completable: CompletableDeferred<R>) -> Unit): R {
    val completable = CompletableDeferred<R>()
    block(completable)
    return completable.await()
}
