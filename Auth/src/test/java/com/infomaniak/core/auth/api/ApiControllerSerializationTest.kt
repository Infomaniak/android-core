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
package com.infomaniak.core.auth.api

import com.infomaniak.core.login.ApiToken
import com.infomaniak.core.network.api.ApiController
import com.infomaniak.core.network.models.ApiResponse
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.Headers
import okio.Buffer
import org.junit.Test

class ApiControllerSerializationTest {
    @Test
    fun requestBody_usesKotlinxSerializedNames() {
        val token = ApiToken("access-token", "refresh-token", "Bearer", userId = 42)
        val buffer = Buffer()

        ApiController.generateRequestBody(token).writeTo(buffer)

        val requestJson = Json.parseToJsonElement(buffer.readUtf8()).jsonObject
        requestJson["access_token"] shouldBe JsonPrimitive("access-token")
        requestJson["refresh_token"] shouldBe JsonPrimitive("refresh-token")
        requestJson.containsKey("expiresAt") shouldBe false
    }

    @Test
    fun apiResponse_usesKotlinxSerializationRegardlessOfLegacyFlag() {
        val response = ApiController.createApiResponse<ApiResponse<ApiToken>>(
            useKotlinxSerialization = false,
            bodyResponse = """{"result":"success","data":{"access_token":"access-token","token_type":"Bearer","user_id":42}}""",
            headers = Headers.headersOf()
        )

        response.data shouldBe ApiToken("access-token", tokenType = "Bearer", userId = 42)
    }
}
