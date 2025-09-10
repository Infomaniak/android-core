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
package com.infomaniak.core.ksuite.myksuite.ui.data

import android.content.Context

abstract class MyKSuiteDataManager {

    abstract var myKSuite: MyKSuiteData?
    protected abstract val currentUserId: Int

    abstract suspend fun fetchData(): MyKSuiteData?

    private var myKSuiteDatabase: MyKSuiteDatabase? = null

    fun initDatabase(appContext: Context) {
        myKSuiteDatabase = MyKSuiteDatabase.getDatabase(appContext)
    }

    suspend fun requestKSuiteData(id: Int? = null) {
        myKSuite = id?.let { getKSuiteData(it) } ?: getKSuiteDataByUser()
    }

    suspend fun upsertKSuiteData(kSuiteData: MyKSuiteData) {
        myKSuite = kSuiteData
        myKSuiteDatabase?.myKSuiteDataDao()?.upsert(kSuiteData.apply { userId = this@MyKSuiteDataManager.currentUserId })
    }

    suspend fun deleteData(userId: Int) {
        (getKSuiteDataByUser(userId) ?: myKSuite)?.let { myKSuiteDatabase?.myKSuiteDataDao()?.delete(it) }
        myKSuite = null
    }

    private suspend fun getKSuiteData(id: Int) = myKSuiteDatabase?.myKSuiteDataDao()?.findById(id)

    private suspend fun getKSuiteDataByUser(userId: Int? = null): MyKSuiteData? {
        return myKSuiteDatabase?.myKSuiteDataDao()?.findByUserId(userId ?: currentUserId)
    }
}
