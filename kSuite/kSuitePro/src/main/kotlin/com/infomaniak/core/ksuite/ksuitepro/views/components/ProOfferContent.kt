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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
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
import com.infomaniak.core.ksuite.ksuitepro.utils.KSuiteProUiUtils.color
import com.infomaniak.core.R as RCore

@Composable
fun ProOfferContent(
    kSuite: KSuite,
    isAdmin: Boolean,
    onClick: () -> Unit,
) {

    val resources = LocalResources.current
    val title = computeTitle(kSuite)
    val description = computeDescription(kSuite)
    val features = computeFeatures(kSuite)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val paddedModifier = Modifier.padding(horizontal = Margin.Large)

        Image(
            imageVector = ImageVector.vectorResource(R.drawable.illu_ksuite_header),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Margin.Large))
        Text(
            modifier = paddedModifier,
            text = title,
            textAlign = TextAlign.Center,
            style = Typography.h2,
            color = resources.color(R.color.kSuitePrimaryText),
        )

        Spacer(Modifier.height(Margin.Medium))
        Text(
            modifier = paddedModifier,
            text = description,
            style = Typography.bodyRegular,
            color = resources.color(R.color.kSuiteSecondaryText),
        )

        Spacer(Modifier.height(Margin.Medium))
        ProFeatures(paddedModifier, features)

        Spacer(Modifier.height(Margin.Large))
        Text(
            modifier = paddedModifier,
            text = stringResource(if (isAdmin) R.string.kSuiteUpgradeDetails else R.string.kSuiteUpgradeDetailsContactAdmin),
            style = Typography.bodyRegular,
            color = resources.color(R.color.kSuiteSecondaryText),
        )

        Spacer(Modifier.height(Margin.Huge))
        Button(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = resources.color(R.color.kSuiteButtonBackground),
                contentColor = resources.color(R.color.kSuitePrimaryText),
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
    val featureModifier = Modifier.fillMaxWidth()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        features.forEach { ProFeature(modifier = featureModifier, feature = it) }
        ProFeature(modifier = featureModifier, feature = ProFeature.More)
    }
}

@Composable
private fun ColumnScope.ProFeature(
    modifier: Modifier = Modifier,
    feature: ProFeature,
) {
    val resources = LocalResources.current
    Row(
        modifier = modifier.align(Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.size(Dimens.iconSize),
            painter = painterResource(feature.icon),
            contentDescription = null,
        )
        Spacer(Modifier.width(Margin.Mini))
        Text(
            modifier = modifier,
            text = stringResource(feature.title),
            style = Typography.bodyRegular,
            color = resources.color(R.color.kSuiteSecondaryText),
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

private fun computeFeatures(kSuite: KSuite): List<ProFeature> {
    return when (kSuite) {
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
