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
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi

/**
 * This function will end if either:
 * 1. the [Completable] passed to the [block] has its [Completable.complete] function called.
 * 2. the passed [block] and all the coroutines launched in its scope are complete.
 *
 * In the case `a` (call to [Completable.complete]), [block] will be cancelled, along with
 * all its child coroutines.
 */
suspend fun <R> completableScope(
    block: suspend CoroutineScope.(
        completable: Completable<R>
    ) -> R
): R {
    val deferred = CompletableDeferred<R>()
    return raceOf(
        { deferred.await() },
        { block(Completable(deferred)) },
    )
}

@JvmInline
value class Completable<T> internal constructor(private val deferred: CompletableDeferred<T>) {

    suspend fun complete(value: T): Nothing {
        deferred.complete(value)
        awaitCancellation()
    }
}
