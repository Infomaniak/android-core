/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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

package com.infomaniak.lib.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infomaniak.lib.core.utils.FormatterFileSize.formatShortFileSize
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormatterFileSizeTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `1_890 short IEC`() {
        val result = context.formatShortFileSize(bytes = 1_890L, valueOnly = true, shortValue = true, iecUnits = true)
        println("=== Result is : [$result] ===")
        assert(result == "1.8")
    }

    @Test
    fun `1_890 long IEC`() {
        val result = context.formatShortFileSize(bytes = 1_890L, valueOnly = true, shortValue = false, iecUnits = true)
        println("=== Result is : [$result] ===")
        assert(result == "1.85")
    }

    @Test
    fun `1_890 short SI`() {
        val result = context.formatShortFileSize(bytes = 1_890L, valueOnly = true, shortValue = true, iecUnits = false)
        println("=== Result is : [$result] ===")
        assert(result == "1.9")
    }

    @Test
    fun `1_890 long SI`() {
        val result = context.formatShortFileSize(bytes = 1_890L, valueOnly = true, shortValue = false, iecUnits = false)
        println("=== Result is : [$result] ===")
        assert(result == "1.89")
    }
}
