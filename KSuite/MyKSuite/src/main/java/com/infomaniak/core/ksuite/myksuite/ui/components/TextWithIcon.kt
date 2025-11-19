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
package com.infomaniak.core.ksuite.myksuite.ui.components

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
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteTheme

/** This component allows to put an icon on any line of the text, thanks to the [iconLine] parameter
 *  This code comes from [here](https://stackoverflow.com/questions/70708056/how-to-centrally-align-icon-to-first-line-of-a-text-component-in-compose/71312465#71312465)
 */
@Composable
internal fun TextWithIcon(
    modifier: Modifier = Modifier,
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
            modifier = modifier
                .padding(start = icon.defaultWidth + iconRightPadding)
                .drawBehind {
                    with(painter) {
                        translate(
                            left = lineLeft - imageSize.width - rightPadding,
                            top = lineTop + (lineBottom - lineTop) / 2 - imageSize.height / 2,
                        ) {
                            draw(size = intrinsicSize, colorFilter = ColorFilter.tint(iconTint))
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
