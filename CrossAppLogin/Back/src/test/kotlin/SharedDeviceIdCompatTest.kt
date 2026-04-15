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
@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdResync
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdStorage
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import org.junit.Test
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SharedDeviceIdCompatTest {

    private val v1TestObject = V1(androidId = Random.nextLong().toHexString(), uuid = Uuid.random().toByteArray())
    private val v2TestObject = V2(
        androidId = Random.nextLong().toHexString(),
        uuid = Uuid.random().toByteArray(),
        packageNamesStatuses = mapOf(
            "something" to SharedDeviceIdResync.SyncStatus.Synced,
        )
    )
    private val latestTestObject = SharedDeviceIdStorage.SharedDeviceId(
        androidId = Random.nextLong().toHexString(),
        uuid = Uuid.random().toByteArray(),
        packageNamesStatuses = mapOf(
            "something" to SharedDeviceIdResync.SyncStatus.Synced,
        ),
    )

    @Test fun `v1 to v2`() = testRoundTripProtobufCompat<V1, V2>(v1TestObject)
    @Test fun `v1 to current`() = testRoundTripProtobufCompat<V1, SharedDeviceIdStorage.SharedDeviceId>(v1TestObject)
    @Test fun `v2 to current`() = testRoundTripProtobufCompat<V2, SharedDeviceIdStorage.SharedDeviceId>(v2TestObject)
    @Test fun `latest to v2`() = testRoundTripProtobufCompat<SharedDeviceIdStorage.SharedDeviceId, V2>(latestTestObject)

    private inline fun <reified T1, reified T2> testRoundTripProtobufCompat(objectToTest: T1) {
        val v1Bytes = ProtoBuf.encodeToByteArray(objectToTest)
        val v2: T2 = ProtoBuf.decodeFromByteArray(v1Bytes)
        val v2Bytes = ProtoBuf.encodeToByteArray(v2)
        val v1FromV2: T1 = ProtoBuf.decodeFromByteArray(v2Bytes)
        val v1BytesAfterRoundTrip = ProtoBuf.encodeToByteArray(v1FromV2)
        if ((v1BytesAfterRoundTrip contentEquals v1Bytes).not()) {
            v1BytesAfterRoundTrip.asList() shouldBe v1Bytes.asList()
        }
    }

    @Suppress("unused") // Properties used by serialization in tests
    @Serializable
    internal class V1(
        @ProtoNumber(1) val androidId: String,
        @ProtoNumber(2) val uuid: ByteArray,
    )

    @Suppress("unused") // Properties used by serialization in tests
    @Serializable
    internal class V2(
        @ProtoNumber(1) val androidId: String,
        @ProtoNumber(2) val uuid: ByteArray,
        @ProtoNumber(3) val syncRequesterPackageName: String? = null,
        @ProtoNumber(4) val packageNamesStatuses: Map<String, SharedDeviceIdResync.SyncStatus> = emptyMap(),
    )
}
