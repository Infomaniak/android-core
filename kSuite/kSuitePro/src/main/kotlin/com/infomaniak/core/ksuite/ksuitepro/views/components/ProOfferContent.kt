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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.ksuite.data.KSuite
import com.infomaniak.core.ksuite.ksuitepro.R
import com.infomaniak.core.ksuite.ksuitepro.data.ProFeature
import com.infomaniak.core.R as RCore

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
            text = stringResource(feature.title),
            style = Typography.bodyRegular,
            color = colorResource(R.color.kSuiteSecondaryText),
        )
    }
}

@Composable
private fun computeTitle(kSuite: KSuite): String {
    val resId = when (kSuite) {
        KSuite.ProFree -> R.string.kSuiteStandardOfferTitle
        KSuite.ProStandard -> R.string.kSuiteBusinessOfferTitle
        else -> R.string.kSuiteEnterpriseOfferTitle
    }
    return stringResource(resId)
}

@Composable
private fun computeDescription(kSuite: KSuite): String {
    val resId = when (kSuite) {
        KSuite.ProFree -> R.string.kSuiteStandardOfferDescription
        KSuite.ProStandard -> R.string.kSuiteBusinessOfferDescription
        else -> R.string.kSuiteEnterpriseOfferDescription
    }
    return stringResource(resId)
}

private fun computeFeatures(kSuite: KSuite): List<ProFeature> = when (kSuite) {
    KSuite.ProFree -> listOf(
        ProFeature.StandardStorage,
        ProFeature.StandardChat,
        ProFeature.StandardMail,
        ProFeature.StandardEuria,
    )
    KSuite.ProStandard -> listOf(
        ProFeature.BusinessStorage,
        ProFeature.BusinessChat,
        ProFeature.BusinessDrive,
        ProFeature.BusinessSecurity,
    )
    else -> listOf(
        ProFeature.EnterpriseStorage,
        ProFeature.EnterpriseChat,
        ProFeature.EnterpriseFunctionality,
        ProFeature.EnterpriseMicrosoft,
    )
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface {
        ProOfferContent(
            kSuite = KSuite.ProFree,
            isAdmin = false,
            onClick = {},
        )
    }
}
