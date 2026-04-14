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
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginService.IpcMessageWhat
import io.kotest.matchers.shouldBe
import org.junit.Test

class IpcMessageWhatTest {

    @Test
    fun `enum ordinals must be stable`() {
        stableOrdinalsToEnums.forEach { (stableOrdinal, enumEntry) ->
            enumEntry.ordinal shouldBe stableOrdinal
        }
    }

    @Test
    fun `All enum entries must be tested`() {
        stableOrdinalsToEnums.size shouldBe IpcMessageWhat.entries.size
    }

    private val stableOrdinalsToEnums = listOf(
         0 to IpcMessageWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS,
         1 to IpcMessageWhat.GET_SHARED_DEVICE_ID,
         2 to IpcMessageWhat.SYNC_SHARED_DEVICE_ID,
         3 to IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REQUEST,
         4 to IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REPORT,
        // Add future entries above this comment.
    )
}
