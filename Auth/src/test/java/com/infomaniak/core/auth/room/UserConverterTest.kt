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
package com.infomaniak.core.auth.room

import com.infomaniak.core.auth.models.OrganizationAccount
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLink
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.Phone
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserConverterTest {
    private val converter = UserConverter()
    @Test
    fun organizations_preserveSerializedFieldNamesAndAllEnumValues() {
        val organizations = ArrayList(OrganizationAccount.Type.entries.map { type ->
            OrganizationAccount(1, "Organization", type, true, false, true, false, true, "company")
        })

        val json = converter.organizationsToJson(organizations)
        val firstOrganization = Json.parseToJsonElement(json).jsonArray.first().jsonObject
        firstOrganization["no_access"] shouldBe JsonPrimitive(true)
        firstOrganization["type"] shouldBe JsonPrimitive("owner")
        converter.toOrganizationAccount(json) shouldBe organizations
        converter.toOrganizationAccount("[]") shouldBe arrayListOf<OrganizationAccount>()
    }

    @Test
    fun organizations_rejectNullAndInvalidData() {
        shouldThrow<IllegalArgumentException> { converter.toOrganizationAccount(null) }
        shouldThrow<SerializationException> { converter.toOrganizationAccount("null") }
        shouldThrow<SerializationException> { converter.toOrganizationAccount("[{}]") }
    }

    @Test
    fun card_preservesSerializedFieldNamesAndAllLinkTypes() {
        val card = Card(
            firstName = "First",
            lastName = "Last",
            email = "example@example.com",
            phone = "+41000000000",
            company = "Company",
            avatarUrl = "https://example.com/avatar",
            links = CardLinkType.entries.map { CardLink(it, "https://example.com") }
        )

        val json = converter.cardToJson(card).orEmpty()
        val serializedCard = Json.parseToJsonElement(json).jsonObject
        serializedCard["first_name"] shouldBe JsonPrimitive("First")
        serializedCard["avatar_url"] shouldBe JsonPrimitive("https://example.com/avatar")
        converter.toCard(json) shouldBe card
    }

    @Test
    fun nullableConverters_preserveNull() {
        converter.cardToJson(null).shouldBeNull()
        converter.toCard(null).shouldBeNull()
        converter.toCard("null").shouldBeNull()
        converter.phonesToJson(null).shouldBeNull()
        converter.toPhones(null).shouldBeNull()
        converter.toPhones("null").shouldBeNull()
    }

    @Test
    fun phones_discardOnlyNullEntriesAndMissingOrNullPhoneValues() {
        val phones = converter.toPhones(
            """[null, {}, {"phone":null}, {"phone":"+41000000000"}, {"phone":""}]"""
        )

        phones shouldBe listOf(Phone("+41000000000"), Phone(""))
        phones.hashCode() shouldBe listOf(Phone("+41000000000"), Phone("")).hashCode()
        converter.toPhones("""[null, {}, {"phone":null}]""") shouldBe emptyList()
        converter.toPhones("[]") shouldBe emptyList()
    }

    @Test
    fun phones_preserveInheritedMetadataAndIgnoreUnknownFields() {
        val phones = converter.toPhones(
            """[{"phone":"+41000000000","id":7,"created_at":123,"reminder":true,"checked":true,"type":"mobile","extra":1}]"""
        ).shouldNotBeNull()
        val phone = phones.single()

        phone.id shouldBe 7
        phone.createdAt shouldBe 123L
        phone.reminder shouldBe true
        phone.checked shouldBe true
        phone.type shouldBe "mobile"

        val restored = converter.toPhones(converter.phonesToJson(phones)).shouldNotBeNull().single()
        restored shouldBe phone
    }

    @Test
    fun converters_rejectOtherMalformedData() {
        shouldThrow<SerializationException> { converter.toCard("{}") }
        shouldThrow<SerializationException> { converter.toPhones("[") }
        shouldThrow<IllegalArgumentException> { converter.toPhones("{}") }
        shouldThrow<IllegalArgumentException> { converter.toPhones("[42]") }
        shouldThrow<SerializationException> { converter.toPhones("""[{"phone":{}}]""") }
        shouldThrow<SerializationException> { converter.toPhones("""[{"phone":"valid","id":null}]""") }
    }
}
