/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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

import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = "User", columnName = "doubleAuth")
@DeleteColumn(tableName = "User", columnName = "doubleAuthMethod")
@DeleteColumn(tableName = "User", columnName = "emailReminderValidate")
@DeleteColumn(tableName = "User", columnName = "emailValidate")
@DeleteColumn(tableName = "User", columnName = "phoneReminderValidate")
class UserV2Migration : AutoMigrationSpec

@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_has_recovery_email",
    toColumnName = "preferences_security_has_recovery_email"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_has_valid_phone",
    toColumnName = "preferences_security_has_valid_phone"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_email_validated_at",
    toColumnName = "preferences_security_email_validated_at"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_sms_phone",
    toColumnName = "preferences_secusms_phonephone"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_infomaniak_application",
    toColumnName = "preferences_security_infomaniak_application"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_double_auth",
    toColumnName = "preferences_securidouble_authone"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_remaining_rescue_code",
    toColumnName = "preferences_security_remaining_rescue_code"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_last_login_at",
    toColumnName = "preferences_securitylast_login_ate"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_date_last_changed_password",
    toColumnName = "preferences_security_date_last_changed_password"
)
@RenameColumn(
    tableName = "User",
    fromColumnName = "preferences_security_double_auth_method",
    toColumnName = "preferences_security_double_auth_method"
)
class UserV3Migration : AutoMigrationSpec
