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
package com.infomaniak.core.avatar

import kotlin.test.Test
import kotlin.test.assertEquals

class InitialsUtilsTest {

    //region computeInitials - nominal cases
    @Test
    fun `first and last name give two initials`() {
        assertEquals("JD", "John Doe".computeInitials())
    }

    @Test
    fun `single name gives a single initial`() {
        assertEquals("J", "John".computeInitials())
    }

    @Test
    fun `initials are uppercased`() {
        assertEquals("JD", "john doe".computeInitials())
    }

    @Test
    fun `only the first two words are used`() {
        assertEquals("JD", "John Doe Smith".computeInitials())
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals("JD", "   John Doe   ".computeInitials())
    }

    @Test
    fun `repeated whitespace between words is collapsed`() {
        assertEquals("JD", "John     Doe".computeInitials())
    }

    @Test
    fun `tabs and line breaks are treated as word separators`() {
        assertEquals("JD", "John\tDoe".computeInitials())
        assertEquals("JD", "John\nDoe".computeInitials())
    }

    @Test
    fun `accented letters are supported`() {
        assertEquals("ÉM", "éric müller".computeInitials())
    }

    @Test
    fun `non latin alphabets are supported`() {
        assertEquals("山太", "山田 太郎".computeInitials())
    }
    //endregion

    //region computeInitials - empty and blank inputs
    @Test
    fun `empty string gives no initials`() {
        assertEquals("", "".computeInitials())
    }

    @Test
    fun `blank string gives no initials`() {
        assertEquals("", "     ".computeInitials())
    }

    @Test
    fun `whitespace only string made of tabs and line breaks gives no initials`() {
        assertEquals("", "\t\n ".computeInitials())
    }
    //endregion

    //region computeInitials - punctuation and control characters
    @Test
    fun `leading punctuation is skipped`() {
        assertEquals("JD", ".john -doe".computeInitials())
    }

    @Test
    fun `punctuation inside a word is skipped`() {
        assertEquals("JD", "j.ohn d-oe".computeInitials())
    }

    @Test
    fun `control characters are skipped`() {
        assertEquals("JD", "\u0000John \u0007Doe".computeInitials())
    }

    @Test
    fun `first word made only of punctuation falls back to that punctuation`() {
        assertEquals("?", "???".computeInitials())
    }

    @Test
    fun `last name made only of punctuation is dropped`() {
        assertEquals("J", "John ???".computeInitials())
    }

    @Test
    fun `first name made only of punctuation still keeps a real last initial`() {
        assertEquals("?D", "??? Doe".computeInitials())
    }
    //endregion

    //region computeInitials - email addresses used as names
    @Test
    fun `simple email address gives the first letter of the local part`() {
        assertEquals("J", "john@example.com".computeInitials())
    }

    @Test
    fun `dotted email address ignores the dot`() {
        assertEquals("J", "john.doe@example.com".computeInitials())
    }

    @Test
    fun `plus addressed email gives the first letter of the local part`() {
        assertEquals("J", "john.doe+newsletter@example.com".computeInitials())
    }

    @Test
    fun `email starting with punctuation skips it`() {
        assertEquals("J", "_john@example.com".computeInitials())
    }

    @Test
    fun `email local part starting with a digit keeps the digit`() {
        assertEquals("1", "123@example.com".computeInitials())
    }
    //endregion

    //region computeFirstAndLastName
    @Test
    fun `first and last name are split on the first space`() {
        assertEquals("John" to "Doe", "John Doe".computeFirstAndLastName())
    }

    @Test
    fun `everything after the first space is the last name`() {
        assertEquals("John" to "Doe Smith", "John Doe Smith".computeFirstAndLastName())
    }

    @Test
    fun `a single word has no last name`() {
        assertEquals("John" to "", "John".computeFirstAndLastName())
    }

    @Test
    fun `an empty string has no first nor last name`() {
        assertEquals("" to "", "".computeFirstAndLastName())
    }

    @Test
    fun `a blank string has no first nor last name`() {
        assertEquals("" to "", "    ".computeFirstAndLastName())
    }

    @Test
    fun `surrounding and repeated whitespace is normalized before splitting`() {
        assertEquals("John" to "Doe", "  John    Doe  ".computeFirstAndLastName())
    }

    @Test
    fun `an email address is a single word`() {
        assertEquals("john.doe@example.com" to "", "john.doe@example.com".computeFirstAndLastName())
    }
    //endregion
}
