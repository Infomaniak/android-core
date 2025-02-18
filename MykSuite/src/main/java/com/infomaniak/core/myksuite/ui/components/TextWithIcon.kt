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
package com.infomaniak.core.myksuite.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

/** This component allows to put an icon on any line of the text, thanks to the [iconLine] parameter
 *
 *  IMPORTANT: You need to define the passed modifier's startPadding as at least the sum of [icon]'s horizontal dp size and
 *  [iconRightPadding] to display correctly the icon.
 */
@Composable
internal fun TextWithIcon(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    color: Color = Color.Unspecified,
    style: TextStyle,
    iconRightPadding: Dp = 0.dp,
    iconLine: Int = 0,
    iconTint: Color = Color.Black,
) {
    val painter = rememberVectorPainter(image = icon)
    var lineTop = 0f
    var lineBottom = 0f
    var lineLeft = 0f
    with(LocalDensity.current) {
        val imageSize = Size(icon.defaultWidth.toPx(), icon.defaultHeight.toPx())
        val rightPadding = iconRightPadding.toPx()
        Text(
            text = text,
            color = color,
            style = style,
            onTextLayout = { layoutResult ->
                val nbLines = layoutResult.lineCount
                if (nbLines > iconLine) {
                    lineTop = layoutResult.getLineTop(iconLine)
                    lineBottom = layoutResult.getLineBottom(iconLine)
                    lineLeft = layoutResult.getLineLeft(iconLine)
                }
            },
            modifier = modifier.drawBehind {
                with(painter) {
                    translate(
                        left = lineLeft - imageSize.width - rightPadding,
                        top = lineTop + (lineBottom - lineTop) / 2 - imageSize.height / 2,
                    ) {
                        draw(painter.intrinsicSize, colorFilter = ColorFilter.tint(iconTint))
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {

    fun getLoremText(words: Int) = LoremIpsum(words).values.joinToString(separator = " ")

    MyKSuiteTheme {
        val localColors = LocalMyKSuiteColors.current
        Surface {
            TextWithIcon(
                // The icon's horizontal size is 16dp, the iconRightPadding is 12dp, so here by passing 32dp as the Text padding
                // it will result in a 32 - (16 + 12) = 4dp start padding for the icon
                modifier = Modifier.padding(start = Margin.Huge),
                text = getLoremText(35), icon = ImageVector.vectorResource(R.drawable.ic_circle_i),
                color = localColors.primaryTextColor,
                style = Typography.bodyRegular,
                iconRightPadding = Margin.Small,
                iconLine = 0,
                iconTint = localColors.iconColor,
            )
        }
    }
}
