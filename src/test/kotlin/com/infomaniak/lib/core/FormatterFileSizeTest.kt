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
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infomaniak.lib.core.utils.FormatterFileSize.formatShortFileSize
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class FormatterFileSizeTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testAllFormatterFileSizePossibilities() {

        val bytesList = listOf(1_892L)
        val iecUnitsLists = listOf(true, false)
        val shortValueList = listOf(true, false)
        val valueOnlyList = listOf(true, false)
        val localeList = listOf(Locale.ENGLISH, Locale.FRENCH)
        val expectedList = mutableListOf(
            "1.8",
            "1.8",
            "1.8 kB",
            "1.8 Ko",
            "1.85",
            "1.85",
            "1.85 kB",
            "1.85 Ko",
            "1.9",
            "1.9",
            "1.9 kB",
            "1.9 Ko",
            "1.89",
            "1.89",
            "1.89 kB",
            "1.89 Ko",
        )

        bytesList.forEach { bytes ->
            iecUnitsLists.forEach { iecUnits ->
                shortValueList.forEach { shortValue ->
                    valueOnlyList.forEach { valueOnly ->
                        localeList.forEach { locale ->

                            val localizedContext = createLocalizedContext(context, locale)
                            val result = localizedContext.formatShortFileSize(bytes, valueOnly, shortValue, iecUnits)

                            println(
                                "===> Result is ${"[$result]".padStart(length = 9)}. " +
                                        "Parameters were [bytes: $bytes], " +
                                        "[iecUnits: ${"$iecUnits".padStart(length = 5)}], " +
                                        "[shortValue: ${"$shortValue".padStart(length = 5)}], " +
                                        "[valueOnly: ${"$valueOnly".padStart(length = 5)}], " +
                                        "[locale: $locale].",
                            )

                            val expected = expectedList.removeFirstOrNull()
                            assert(result == expected)
                        }
                    }
                }
            }
        }

        assert(expectedList.isEmpty())
    }

    private fun createLocalizedContext(context: Context, locale: Locale): Context {
        val localizedConfiguration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(localizedConfiguration)
    }
}
