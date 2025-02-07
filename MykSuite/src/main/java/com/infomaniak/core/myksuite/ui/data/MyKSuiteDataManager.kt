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
package com.infomaniak.core.myksuite.ui.data

import android.content.Context

abstract class MyKSuiteDataManager {

    abstract var myKSuiteId: Int
    abstract var myKSuite: MyKSuiteData?
    abstract val userId: Int

    private var myKSuiteDatabase: MyKSuiteDatabase? = null

    fun initDatabase(appContext: Context) {
        myKSuiteDatabase = MyKSuiteDatabase.getDatabase(appContext)
    }

    suspend fun requestKSuiteData(id: Int? = null) {
        myKSuite = id?.let { getKSuiteData(it) } ?: getKSuiteDataByUser()
    }

    suspend fun upsertKSuiteData(kSuiteData: MyKSuiteData) {
        myKSuite = kSuiteData
        myKSuiteDatabase?.myKSuiteDataDao()?.upsert(kSuiteData.apply { userId = this@MyKSuiteDataManager.userId })
    }

    suspend fun deleteKSuiteData(kSuiteData: MyKSuiteData) {
        myKSuite = null
        myKSuiteDatabase?.myKSuiteDataDao()?.delete(kSuiteData)
    }

    // TODO remove if not useful
    private suspend fun getKSuiteData(id: Int) = myKSuiteDatabase?.myKSuiteDataDao()?.findById(id)

    private suspend fun getKSuiteDataByUser() = myKSuiteDatabase?.myKSuiteDataDao()?.findByUserId(userId)
}
