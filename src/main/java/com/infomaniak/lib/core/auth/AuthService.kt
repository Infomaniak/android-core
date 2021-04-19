/*
 * Infomaniak Core - Android
 * Copyright (C) 2021 Infomaniak Network SA
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
package com.infomaniak.lib.core.auth

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.google.gson.Gson

/**
 ** AuthService **
 * Messenger service which consists to get users from `CredentialManager` instance on the related app,
 * and return it to the operation owner
 */
class AuthService : Service() {
    companion object {
        private const val USERS_BUNDLE_KEY = "users"
        private const val MSG_SEND_ERROR = 40
        private const val MSG_ASK_USERS = 44
        private const val MSG_SEND_USERS = 45
    }

    private lateinit var messenger: Messenger
    private val gson = Gson()
    private var incomingHandler: Handler = Handler { message ->
        val ownerMessenger = message.replyTo
        when (message.what) {
            MSG_ASK_USERS -> {
                getUsersFromCurrentApp().let { userList ->
                    val userListMessage = Message.obtain().apply {
                        val usersJson = gson.toJson(userList)
                        val bundleToSend = Bundle().apply {
                            putString(USERS_BUNDLE_KEY, usersJson)
                        }
                        this.what = MSG_SEND_USERS
                        this.data = bundleToSend
                    }
                    ownerMessenger.send(userListMessage)
                }
            }
            else -> {
                val errorMessage = Message.obtain().apply {
                    this.what = MSG_SEND_ERROR
                }
                ownerMessenger.send(errorMessage)
                Log.e("AuthMessengerService", "Unknown message received : ${message.what}")
            }
        }
        true
    }

    private fun getUsersFromCurrentApp(): String {
        // Find a way to export Infomaniak Users and un-json them
        // return InfomaniakCore.credentialManager?.getUsers()
        return ""
    }

    override fun onBind(p0: Intent?): IBinder? {
        messenger = Messenger(incomingHandler)
        return messenger.binder
    }
}