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
package com.infomaniak.core.ui.compose.contactcard

import kotlin.test.Test
import kotlin.test.assertEquals

class ContactCardShareExtTest {

    @Test
    fun `VCard file name does not include cache directory name`() {
        assertEquals("John_Doe.vcf", createVCardFileName(firstName = "John", lastName = "Doe"))
    }

    @Test
    fun `VCard file name removes illegal characters`() {
        assertEquals("John_Doe.vcf", createVCardFileName(firstName = "Jo/hn", lastName = "D:oe"))
    }
}
