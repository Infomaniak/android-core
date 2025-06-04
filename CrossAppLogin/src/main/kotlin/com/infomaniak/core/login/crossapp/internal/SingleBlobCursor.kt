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
package com.infomaniak.core.login.crossapp.internal

import android.database.AbstractCursor

internal class SingleBlobCursor(private val data: ByteArray) : AbstractCursor() {

    override fun getCount(): Int = 1

    override fun getColumnNames(): Array<out String?>? = dummyColumnNames

    override fun getBlob(column: Int): ByteArray = data

    override fun getString(column: Int) = unsupported()
    override fun getShort(column: Int) = unsupported()
    override fun getInt(column: Int) = unsupported()
    override fun getLong(column: Int) = unsupported()
    override fun getFloat(column: Int) = unsupported()
    override fun getDouble(column: Int) = unsupported()
    override fun isNull(column: Int) = unsupported()

    private fun unsupported(): Nothing = throw UnsupportedOperationException()

    private companion object {
        private val dummyColumnNames = arrayOf("")
    }
}
