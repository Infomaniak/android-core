/*
 * Infomaniak Mail - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.PrimaryKey
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.infomaniak.lib.core.models.OrganizationAccount
import com.infomaniak.lib.core.models.user.Email
import com.infomaniak.lib.core.models.user.Phone
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.models.user.preferences.Preferences
import com.infomaniak.lib.login.ApiToken
import java.lang.reflect.Type

class CustomUserDeserializer: JsonDeserializer<User> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): User {
        val jsonObject = (json as JsonObject).asMap()
        val isAvatarDefault = jsonObject["is_avatar_default"]?.asBoolean ?: false

        val a = User(
            displayName = jsonObject["displayName"]?.asString,
            firstname = jsonObject["firstname"]!!.asString,
            lastname = jsonObject["lastname"]!!.asString,
            email = jsonObject["email"]!!.asString,
            avatar = if (isAvatarDefault) null else jsonObject["email"]?.asString,
            login = jsonObject["login"]!!.asString,
            isStaff = jsonObject["login"]!!.asBoolean,
            preferences = jsonObject["preferences"]!!.,
            phones = "",
            emails = "",
            apiToken = "",
            organizations = "",
        )
    }
}
