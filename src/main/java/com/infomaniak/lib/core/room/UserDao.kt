/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.infomaniak.lib.core.models.user.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM user")
    fun getAllSync(): List<User>

    @Query("SELECT COUNT(id) FROM user")
    fun count(): Int

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getFirst(): User?

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    suspend fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE id LIKE (:id) LIMIT 1")
    suspend fun findById(id: Int): User?

    @Query("SELECT * FROM user WHERE firstname LIKE (:firstName) AND lastname LIKE (:lastName) LIMIT 1")
    suspend fun findByName(firstName: String, lastName: String): User?

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}