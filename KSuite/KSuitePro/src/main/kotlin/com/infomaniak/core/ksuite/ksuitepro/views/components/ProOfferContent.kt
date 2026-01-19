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
package com.infomaniak.core.ksuite.ksuitepro.views.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ksuite.data.KSuite
import com.infomaniak.core.ksuite.ksuitepro.R
import com.infomaniak.core.ksuite.ksuitepro.data.ProFeature
import com.infomaniak.core.common.R as RCore

@Composable
fun ProOfferContent(
    kSuite: KSuite,
    isAdmin: Boolean,
    onClick: () -> Unit,
) {

    val title = computeTitle(kSuite)
    val description = computeDescription(kSuite)
    val features = computeFeatures(kSuite)

    Column(
        modifier = Modifier
            .background(colorResource(R.color.kSuiteContentBackground))
            .verticalScroll(rememberScrollState()),
    ) {

        Image(
            imageVector = ImageVector.vectorResource(R.drawable.illu_ksuite_header),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Margin.Large))

        val paddedModifier = Modifier.padding(horizontal = Margin.Large)

        Text(
            modifier = paddedModifier.fillMaxWidth(),
            text = title,
            textAlign = TextAlign.Center,
            style = Typography.h2,
            color = colorResource(R.color.kSuitePrimaryText),
        )
        Spacer(Modifier.height(Margin.Medium))

        Text(
            modifier = paddedModifier,
            text = description,
            style = Typography.bodyRegular,
            color = colorResource(R.color.kSuiteSecondaryText),
        )
        Spacer(Modifier.height(Margin.Medium))

        ProFeatures(paddedModifier, features)
        Spacer(Modifier.height(Margin.Large))

        Text(
            modifier = paddedModifier,
            text = stringResource(if (isAdmin) R.string.kSuiteUpgradeDetails else R.string.kSuiteUpgradeDetailsContactAdmin),
            style = Typography.bodyRegular,
            color = colorResource(R.color.kSuiteSecondaryText),
        )
        Spacer(Modifier.height(Margin.Huge))

        Button(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.kSuiteButtonBackground),
                contentColor = colorResource(R.color.kSuitePrimaryText),
            ),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            onClick = onClick,
        ) {
            Text(text = stringResource(RCore.string.buttonClose), style = Typography.bodyMedium)
        }
        Spacer(Modifier.height(Margin.Large))
    }
}

@Composable
private fun ProFeatures(
    modifier: Modifier,
    features: List<ProFeature>,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        features.forEach { ProFeature(feature = it) }
        ProFeature(feature = ProFeature.More)
    }
}

@Composable
private fun ColumnScope.ProFeature(
    feature: ProFeature,
) {
    Row(
        modifier = Modifier.align(Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        Image(
            modifier = Modifier.size(Dimens.iconSize),
            painter = painterResource(feature.icon),
            contentDescription = null,
        )
        Text(
            modifier = Modifier,
            text = computeBoldString(completeStringRes = feature.title, boldSubstringRes = feature.bold),
            style = Typography.bodyRegular,
            color = colorResource(R.color.kSuiteSecondaryText),
        )
    }
}

@Composable
private fun computeTitle(kSuite: KSuite): String {
    val resId = when (kSuite) {
        KSuite.Pro.Free -> R.string.kSuiteStandardOfferTitle
        KSuite.Pro.Standard -> R.string.kSuiteBusinessOfferTitle
        else -> R.string.kSuiteEnterpriseOfferTitle
    }
    return stringResource(resId)
}

@Composable
private fun computeDescription(kSuite: KSuite): String {
    val resId = when (kSuite) {
        KSuite.Pro.Free -> R.string.kSuiteStandardOfferDescription
        KSuite.Pro.Standard -> R.string.kSuiteBusinessOfferDescription
        else -> R.string.kSuiteEnterpriseOfferDescription
    }
    return stringResource(resId)
}

@Composable
private fun computeBoldString(
    @StringRes completeStringRes: Int,
    @StringRes boldSubstringRes: Int?,
): AnnotatedString {

    val completeString = stringResource(completeStringRes)
    val range = boldSubstringRes?.let { stringResource(it) }?.toRegex()?.find(completeString)?.range

    return buildAnnotatedString {
        if (range == null) {
            append(completeString)
        } else {
            completeString.forEachIndexed { index, char ->
                if (index in range) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(char) }
                } else {
                    append(char)
                }
            }
        }
    }
}

private fun computeFeatures(kSuite: KSuite): List<ProFeature> = when (kSuite) {
    KSuite.Pro.Free -> listOf(
        ProFeature.StorageStandard,
        ProFeature.ChatStandard,
        ProFeature.Mail,
        ProFeature.Euria,
    )
    KSuite.Pro.Standard -> listOf(
        ProFeature.StorageBusiness,
        ProFeature.ChatBusiness,
        ProFeature.DriveBusiness,
        ProFeature.Security,
        ProFeature.Microsoft,
    )
    else -> listOf(
        ProFeature.StorageEnterprise,
        ProFeature.ChatEnterprise,
        ProFeature.DriveEnterprise,
        ProFeature.Functionalities,
    )
}

@Preview(locale = "fr", name = "(1) Light")
@Preview(locale = "fr", name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewFree() {
    Surface {
        ProOfferContent(
            kSuite = KSuite.Pro.Free,
            isAdmin = false,
            onClick = {},
        )
    }
}

@Preview(locale = "fr")
@Composable
private fun PreviewStandard() {
    Surface {
        ProOfferContent(
            kSuite = KSuite.Pro.Standard,
            isAdmin = true,
            onClick = {},
        )
    }
}

@Preview(locale = "fr")
@Composable
private fun PreviewBusiness() {
    Surface {
        ProOfferContent(
            kSuite = KSuite.Pro.Business,
            isAdmin = true,
            onClick = {},
        )
    }
}
