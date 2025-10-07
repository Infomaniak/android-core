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
package com.infomaniak.core.network

private val host = ApiEnvironment.current.host

val LOGIN_ENDPOINT_URL = "https://login.$host/"
val INFOMANIAK_API = "https://api.$host/2/"
val INFOMANIAK_API_V1 = "https://api.$host/1"

val AUTOLOG_URL = "https://manager.$host/v3/mobile_login"

val SHOP_URL = "https://shop.infomaniak.com/order/" //Should it be host dependent?
val SUPPORT_URL = "https://support.infomaniak.com" //Should it be host dependent?

val MANAGER_URL = "https://manager.${host}/v3/"

val TERMINATE_ACCOUNT_URL = "${MANAGER_URL}ng/profile/user/dashboard?open-terminate-account-modal"

val MATOMO_URL = "https://analytics.$host/matomo.php"
