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
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MyKSuiteData::class],
    version = 1,
    exportSchema = true,
)
abstract class MyKSuiteDatabase : RoomDatabase() {

    abstract fun myKSuiteDataDao(): MyKSuiteDataDao

    companion object {

        @Volatile
        private var INSTANCE: MyKSuiteDatabase? = null

        fun getDatabase(context: Context): MyKSuiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = MyKSuiteDatabase::class.java,
                    name = "my_ksuite_database",
                ).apply {
                    fallbackToDestructiveMigration()
                }.build()

                INSTANCE = instance
                instance
            }
        }
    }
}
