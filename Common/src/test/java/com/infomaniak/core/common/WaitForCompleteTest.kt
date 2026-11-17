/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2026 Infomaniak Network SA
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

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class WaitForCompleteTest {

    @Test
    fun `completeExceptionally does not break structured concurrency`() = runTest(timeout = 1.seconds) {
        assertFailsWith<Error> {
            waitForValue { it.completeExceptionally(Error()) }
        }
    }

    @Test
    fun `resumes with value from callback`() = runTest(timeout = 1.seconds) {
        val value = Any()
        waitForValue {
            delay(.5.seconds)
            it.complete(value)
        } shouldBe value
    }

    @Test
    fun `resumes after callback with value from last expression`() = runTest(timeout = 1.seconds) {
        val value = Any()
        waitForComplete {
            delay(.5.seconds)
            it()
            value
        } shouldBe value
    }
}
