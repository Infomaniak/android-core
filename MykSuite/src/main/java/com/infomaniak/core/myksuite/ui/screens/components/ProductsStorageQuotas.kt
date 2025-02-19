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
package com.infomaniak.core.myksuite.ui.screens.components

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.MyKSuiteTier
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography
import kotlinx.parcelize.Parcelize

@Composable
internal fun ProductsStorageQuotas(
    modifier: Modifier,
    myKSuiteTier: MyKSuiteTier,
    kSuiteProductsWithQuotas: () -> List<KSuiteProductsWithQuotas>,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        kSuiteProductsWithQuotas().forEach { ProductStorageQuota(myKSuiteTier = myKSuiteTier, product = it) }
    }
}

@Composable
private fun ProductStorageQuota(myKSuiteTier: MyKSuiteTier, product: KSuiteProductsWithQuotas) {
    val localColors = LocalMyKSuiteColors.current
    val isUnlimitedMail = myKSuiteTier == MyKSuiteTier.Plus && product is KSuiteProductsWithQuotas.Mail

    Column {
        MyKSuiteTextItem(
            title = product.displayName,
            value = computeQuotasString(isUnlimitedMail, product),
            valueStyle = Typography.bodySmallRegular,
        )

        if (!isUnlimitedMail) {
            Spacer(Modifier.height(Margin.Mini))
            val progressIndicatorHeight = 14.dp
            LinearProgressIndicator(
                modifier = Modifier
                    .height(progressIndicatorHeight)
                    .fillMaxWidth(),
                color = product.getColor(),
                trackColor = localColors.chipBackground,
                strokeCap = StrokeCap.Round,
                gapSize = -progressIndicatorHeight,
                progress = { product.progress },
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun computeQuotasString(isUnlimitedMail: Boolean, product: KSuiteProductsWithQuotas): String {
    return if (isUnlimitedMail) {
        stringResource(R.string.myKSuiteDashboardDataUnlimited)
    } else {
        "${product.usedSize} / ${product.maxSize}"
    }
}

@Parcelize
sealed class KSuiteProductsWithQuotas(
    internal val displayName: String,
    open val usedSize: String,
    open val maxSize: String,
    open val progress: Float,
) : Parcelable {

    class Mail(override val usedSize: String, override val maxSize: String, override val progress: Float) :
        KSuiteProductsWithQuotas("Mail", usedSize, maxSize, progress)

    data class Drive(override val usedSize: String, override val maxSize: String, override val progress: Float) :
        KSuiteProductsWithQuotas("kDrive", usedSize, maxSize, progress)

    @Composable
    fun getColor() = if (this is Mail) LocalMyKSuiteColors.current.mail else LocalMyKSuiteColors.current.drive
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            ProductsStorageQuotas(
                modifier = Modifier.padding(Margin.Medium),
                myKSuiteTier = MyKSuiteTier.Plus,
                kSuiteProductsWithQuotas = {
                    listOf(
                        KSuiteProductsWithQuotas.Mail(usedSize = "0.2 Go", maxSize = "20 Go", progress = 0.01f),
                        KSuiteProductsWithQuotas.Drive(usedSize = "6 Go", maxSize = "15 Go", progress = 0.4f),
                    )
                },
            )
        }
    }
}
