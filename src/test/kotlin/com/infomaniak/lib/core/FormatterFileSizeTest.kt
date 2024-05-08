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
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormatterFileSizeTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `900 is not rounded`() {
        Assert.assertEquals("900", context.formatShortFileSize(bytes = 900L, valueOnly = true))
    }

    @Test
    fun `901 is rounded to 0,XX`() {
        Assert.assertEquals("0.88", context.formatShortFileSize(bytes = 901L, valueOnly = true))
    }

    @Test
    fun `921_600`() {
        Assert.assertEquals("900", context.formatShortFileSize(bytes = 921_600L, valueOnly = true))
    }

    @Test
    fun `921_601`() {
        Assert.assertEquals("0.88", context.formatShortFileSize(bytes = 921_601L, valueOnly = true))
    }

    @Test
    fun `1_890`() {
        Assert.assertEquals("1.8", context.formatShortFileSize(bytes = 1_890L, valueOnly = true))
    }

    @Test
    fun `1_890_000`() {
        Assert.assertEquals("1.8", context.formatShortFileSize(bytes = 1_890_000L, valueOnly = true))
    }

    @Test
    fun `1_890_000_000`() {
        Assert.assertEquals("1.8", context.formatShortFileSize(bytes = 1_890_000_000L, valueOnly = true))
    }

    @Test
    fun `1_890_000_000_000`() {
        Assert.assertEquals("1.7", context.formatShortFileSize(bytes = 1_890_000_000_000L, valueOnly = true))
    }

    @Test
    fun `1_890_000_000_000_000`() {
        Assert.assertEquals("1.7", context.formatShortFileSize(bytes = 1_890_000_000_000_000L, valueOnly = true))
    }

    @Test
    fun `1_890_000_000_000_000_000`() {
        Assert.assertEquals("1.6", context.formatShortFileSize(bytes = 1_890_000_000_000_000_000L, valueOnly = true))
    }
}
