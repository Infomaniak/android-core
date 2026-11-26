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

import com.google.gson.Gson
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserConverterTest {
    private val converter = UserConverter()
    private val gson = Gson()

    @Test
    fun organizations_preserveGsonFieldNamesAndAllEnumValues() {
        val organizations = ArrayList(OrganizationAccount.Type.entries.map { type ->
            OrganizationAccount(1, "Organization", type, true, false, true, false, true, "company")
        })

        converter.toOrganizationAccount(gson.toJson(organizations)) shouldBe organizations
        converter.toOrganizationAccount(converter.organizationsToJson(organizations)) shouldBe organizations
        gson.fromJson(
            converter.organizationsToJson(organizations),
            com.google.gson.JsonElement::class.java
        ) shouldBe gson.toJsonTree(organizations)
        converter.toOrganizationAccount("[]") shouldBe arrayListOf<OrganizationAccount>()
    }

    @Test
    fun organizations_rejectNullAndInvalidData() {
        shouldThrow<IllegalArgumentException> { converter.toOrganizationAccount(null) }
        shouldThrow<SerializationException> { converter.toOrganizationAccount("null") }
        shouldThrow<SerializationException> { converter.toOrganizationAccount("[{}]") }
    }

    @Test
    fun card_preservesGsonFieldNamesAndAllLinkTypes() {
        val card = Card(
            firstName = "First",
            lastName = "Last",
            email = "example@example.com",
            phone = "+41000000000",
            company = "Company",
            avatarUrl = "https://example.com/avatar",
            links = CardLinkType.entries.map { CardLink(it, "https://example.com") }
        )

        converter.toCard(gson.toJson(card)) shouldBe card
        converter.toCard(converter.cardToJson(card)) shouldBe card
        gson.fromJson(
            converter.cardToJson(card),
            com.google.gson.JsonElement::class.java
        ) shouldBe gson.toJsonTree(card)
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
        gson.toJsonTree(restored) shouldBe gson.toJsonTree(phone)
        converter.toPhones(gson.toJson(phones)) shouldBe phones
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
