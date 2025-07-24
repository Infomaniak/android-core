/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
@file:Suppress("NOTHING_TO_INLINE")

package com.infomaniak.lib.core.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.infomaniak.lib.core.models.OrganizationAccount
import com.infomaniak.lib.core.models.user.Email
import com.infomaniak.lib.core.models.user.Phone
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.models.user.preferences.security.AuthDevices
import splitties.init.appCtx

@Database(
    entities = [User::class],
    autoMigrations = [
        AutoMigration(
            from = 1, to = 2,
            spec = UserV2Migration::class
        ),
        AutoMigration(
            from = 2, to = 3,
            spec = UserV3Migration::class
        ),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(UserConverter::class)
abstract class UserDatabase internal constructor() : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        inline operator fun invoke(): UserDatabase = instance

        fun getDatabase(): UserDatabase = instance

        @PublishedApi
        internal val instance = Room.databaseBuilder<UserDatabase>(
            context = appCtx,
            name = "user_database"
        ).apply {
            enableMultiInstanceInvalidation()
            fallbackToDestructiveMigration(dropAllTables = true)
        }.build()
    }
}

class UserConverter {
    private val gson: Gson by lazy { Gson() }

    private val authDevicesType = object : TypeToken<ArrayList<AuthDevices>>() {}.type
    private val emailsType = object : TypeToken<ArrayList<Email>>() {}.type
    private val organizationAccountsType = object : TypeToken<ArrayList<OrganizationAccount>>() {}.type
    private val phonesType = object : TypeToken<ArrayList<Phone>>() {}.type

    @TypeConverter
    fun authDevicesToJson(authDevices: ArrayList<AuthDevices>?): String {
        return gson.toJson(authDevices, authDevicesType)
    }

    @TypeConverter
    fun toauthDevices(json: String?): ArrayList<AuthDevices>? {
        return gson.fromJson(json, authDevicesType)
    }

    @TypeConverter
    fun emailsToJson(emails: ArrayList<Email>?): String {
        return gson.toJson(emails, emailsType)
    }

    @TypeConverter
    fun toEmails(json: String?): ArrayList<Email>? {
        return gson.fromJson(json, emailsType)
    }

    @TypeConverter
    fun organizationsToJson(organizationAccounts: ArrayList<OrganizationAccount>): String {
        return gson.toJson(organizationAccounts, organizationAccountsType)
    }

    @TypeConverter
    fun toOrganizationAccount(json: String?): ArrayList<OrganizationAccount> {
        return gson.fromJson(json, organizationAccountsType)
    }

    @TypeConverter
    fun phonesToJson(phones: ArrayList<Phone>?): String {
        return gson.toJson(phones, phonesType)
    }

    @TypeConverter
    fun toPhones(json: String?): ArrayList<Phone>? {
        return gson.fromJson(json, phonesType)
    }
}
