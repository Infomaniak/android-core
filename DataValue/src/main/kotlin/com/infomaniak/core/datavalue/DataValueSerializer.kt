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
package com.infomaniak.core.datavalue

/**
 * Describes how to convert a value of type [T] to and from the [String] representation that gets persisted by a [DataValue].
 *
 * Implement this for types that are neither primitives, nor [kotlinx.serialization.Serializable] (for instance third-party types
 * you don't own), then pass the implementation to [DataValues.dataValue]:
 *
 * ```kotlin
 * object UnownedTypeSerializer : DataValueSerializer<UnownedType> {
 *     override fun serialize(value: UnownedType): String = when (value) {
 *         UnownedType.A -> "A"
 *         UnownedType.B -> "B"
 *     }
 *
 *     override fun deserialize(value: String): UnownedType = when (value) {
 *         "A" -> UnownedType.A
 *         "B" -> UnownedType.B
 *     }
 * }
 * ```
 *
 * Implementations must be stable across app versions: a value produced by [serialize] has to remain readable by
 * [deserialize] in every future release.
 */
interface DataValueSerializer<T> {

    /** Encodes [value] into the [String] that will be stored. */
    suspend fun serialize(value: T): String

    /** Decodes a previously [serialize]d [String] back into a [T]. */
    suspend fun deserialize(value: String): T
}
