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

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.infomaniak.lib.core.models.Email
import com.infomaniak.lib.core.models.OrganizationAccount
import com.infomaniak.lib.core.models.Phone
import com.infomaniak.lib.core.models.User

@Database(entities = [User::class], version = 1)
@TypeConverters(OrganizationAccountConverter::class)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).apply {
                    enableMultiInstanceInvalidation()
                    fallbackToDestructiveMigration()
                }.build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class OrganizationAccountConverter {
    private val organizationAccountsType = object : TypeToken<ArrayList<OrganizationAccount>>() {}.type
    private val phonesType = object : TypeToken<ArrayList<Phone>>() {}.type
    private val emailsType = object : TypeToken<ArrayList<Email>>() {}.type
    private val gson: Gson by lazy { Gson() }

    @TypeConverter
    fun toOrganizationAccount(json: String): ArrayList<OrganizationAccount> {
        return gson.fromJson(json, organizationAccountsType)
    }

    @TypeConverter
    fun toPhones(json: String): ArrayList<Phone>? {
        return gson.fromJson(json, phonesType)
    }

    @TypeConverter
    fun toEmails(json: String): ArrayList<Email>? {
        return gson.fromJson(json, emailsType)
    }

    @TypeConverter
    fun organizationsToJson(organizationAccounts: ArrayList<OrganizationAccount>): String {
        return gson.toJson(organizationAccounts, organizationAccountsType)
    }

    @TypeConverter
    fun phonesToJson(phones: ArrayList<Phone>?): String {
        return gson.toJson(phones, phonesType)
    }

    @TypeConverter
    fun emailsToJson(emails: ArrayList<Email>?): String {
        return gson.toJson(emails, emailsType)
    }
}