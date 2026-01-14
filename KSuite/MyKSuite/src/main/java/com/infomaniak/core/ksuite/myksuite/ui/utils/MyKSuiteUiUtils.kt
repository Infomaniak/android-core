/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
package com.infomaniak.core.ksuite.myksuite.ui.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import com.infomaniak.core.common.FormatterFileSize.formatShortFileSize
import com.infomaniak.core.ksuite.myksuite.ui.data.AvatarData
import com.infomaniak.core.ksuite.myksuite.ui.data.MyKSuiteData
import com.infomaniak.core.ksuite.myksuite.ui.screens.KSuiteApp
import com.infomaniak.core.ksuite.myksuite.ui.screens.MyKSuiteDashboardScreenData
import com.infomaniak.core.ksuite.myksuite.ui.screens.components.KSuiteProductsWithQuotas
import com.infomaniak.core.ksuite.myksuite.ui.views.MyKSuiteUpgradeBottomSheetDialog

object MyKSuiteUiUtils {

    internal const val DEEPLINK_BASE = "android-app://com.infomaniak.core.ksuite.myksuite"

    fun NavController.openMyKSuiteUpgradeBottomSheet(app: KSuiteApp) {
        NavDeepLinkRequest.Builder
            .fromUri(MyKSuiteUpgradeBottomSheetDialog.getDeeplink(app).toUri())
            .build()
            .also(::navigate)
    }

    /**
     * To compute the correct [userInitialsBackgroundColor] for a user, use the [getBackgroundColorResBasedOnId] method
     * with the appropriate list of colors.
     */
    fun getDashboardData(
        context: Context,
        myKSuiteData: MyKSuiteData,
        userId: Int,
        avatarUri: String?,
        userInitials: String,
        @ColorInt iconColor: Int,
        @ColorInt userInitialsBackgroundColor: Int,
    ): MyKSuiteDashboardScreenData {
        return MyKSuiteDashboardScreenData(
            myKSuiteTier = myKSuiteData.tier,
            email = myKSuiteData.mail.email,
            avatarData = AvatarData(
                id = userId.toString(),
                uri = avatarUri,
                userInitials = userInitials,
                initialsColor = iconColor,
                backgroundColor = userInitialsBackgroundColor,
            ),
            dailySendingLimit = myKSuiteData.mail.dailyLimitSent.toString(),
            kSuiteProductsWithQuotas = getKSuiteQuotasApp(context, myKSuiteData).toList(),
            trialExpiryDate = myKSuiteData.trialExpiryDate,
        )
    }

    private fun getKSuiteQuotasApp(context: Context, myKSuite: MyKSuiteData): Array<KSuiteProductsWithQuotas> {

        fun computeProgress(usedSize: Long, maxSize: Long): Float {
            return if (maxSize == 0L) 0.0f else (usedSize.toDouble() / maxSize.toDouble()).toFloat()
        }

        val mailProduct = with(myKSuite.mail) {
            KSuiteProductsWithQuotas.Mail(
                usedSize = context.formatShortFileSize(usedSize),
                maxSize = context.formatShortFileSize(storageSizeLimit),
                progress = computeProgress(usedSize, storageSizeLimit),
            )
        }

        val driveProduct = with(myKSuite.drive) {
            KSuiteProductsWithQuotas.Drive(
                usedSize = context.formatShortFileSize(usedSize),
                maxSize = context.formatShortFileSize(size),
                progress = computeProgress(usedSize, size),
            )
        }

        return arrayOf(mailProduct, driveProduct)
    }
}
