package com.infomaniak.core.twofactorauth.front.elements

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val shieldKIcon: ImageVector
    get() {
        if (_shieldK != null) return _shieldK!!
        
        _shieldK = ImageVector.Builder(
            name = "shieldk",
            defaultWidth = 23.dp,
            defaultHeight = 26.dp,
            viewportWidth = 23f,
            viewportHeight = 26f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.7185f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(1.02884f, 4.32734f)
                verticalLineTo(12.5052f)
                curveTo(1.02887f, 15.0902f, 1.81218f, 17.6143f, 3.27552f, 19.7452f)
                curveTo(4.73887f, 21.8759f, 6.81355f, 23.5133f, 9.22608f, 24.4413f)
                lineTo(10.3442f, 24.8709f)
                curveTo(11.0888f, 25.1573f, 11.9131f, 25.1573f, 12.6577f, 24.8709f)
                lineTo(13.7758f, 24.4413f)
                curveTo(16.1883f, 23.5133f, 18.263f, 21.8759f, 19.7264f, 19.7452f)
                curveTo(21.1897f, 17.6143f, 21.973f, 15.0902f, 21.973f, 12.5052f)
                verticalLineTo(4.32734f)
                curveTo(21.9751f, 4.0196f, 21.8882f, 3.71782f, 21.7229f, 3.45829f)
                curveTo(21.5575f, 3.19878f, 21.3206f, 2.99256f, 21.0407f, 2.86447f)
                curveTo(18.0333f, 1.55174f, 14.7824f, 0.888899f, 11.5009f, 0.919338f)
                curveTo(8.21954f, 0.888899f, 4.96869f, 1.55174f, 1.96113f, 2.86447f)
                curveTo(1.68132f, 2.99256f, 1.44446f, 3.19878f, 1.27908f, 3.45829f)
                curveTo(1.11368f, 3.71782f, 1.02679f, 4.0196f, 1.02884f, 4.32734f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF))
            ) {
                moveTo(7.07764f, 5.99896f)
                horizontalLineTo(10.4208f)
                verticalLineTo(12.0345f)
                lineTo(12.8638f, 9.22432f)
                horizontalLineTo(16.8901f)
                lineTo(13.8282f, 12.1942f)
                lineTo(17.0669f, 17.7029f)
                horizontalLineTo(13.3782f)
                lineTo(11.6503f, 14.3019f)
                lineTo(10.4208f, 15.4994f)
                verticalLineTo(17.7029f)
                horizontalLineTo(7.07764f)
                verticalLineTo(5.99896f)
                close()
            }
        }.build()
        
        return _shieldK!!
    }

private var _shieldK: ImageVector? = null
