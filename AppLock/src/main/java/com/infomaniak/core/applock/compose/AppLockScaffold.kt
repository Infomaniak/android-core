package com.infomaniak.core.applock.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.applock.R
import com.infomaniak.core.ui.compose.basics.ButtonStyle
import com.infomaniak.core.ui.compose.basics.ButtonType
import com.infomaniak.core.ui.compose.bottomstickybuttonscaffolds.BottomStickyButtonScaffold

@Composable
fun AppLockScaffold(
    topBar: @Composable () -> Unit,
    illustration: @Composable () -> Unit,
    buttonStyle: ButtonStyle,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    BottomStickyButtonScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomButton = { BottomButton(buttonStyle, buttonColors, onUnlock, modifier = it) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            illustration()
            Text(stringResource(R.string.lockAppTitle))
        }
    }
}

@Composable
private fun BottomButton(
    buttonStyle: ButtonStyle,
    buttonColors: ButtonColors,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier.heightIn(min = buttonStyle.height),
        shape = buttonStyle.shape,
        colors = buttonColors,
        onClick = onUnlock,
    ) {
        Text(stringResource(R.string.buttonUnlock))
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            AppLockScaffold(
                topBar = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        Box(
                            modifier = Modifier
                                .size(200.dp, 100.dp)
                                .background(Color.Gray),
                        )
                    }
                },
                illustration = {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .background(Color.Gray),
                    )
                },
                buttonStyle = ButtonType.Drive,
                onUnlock = {},
            )
        }
    }
}
