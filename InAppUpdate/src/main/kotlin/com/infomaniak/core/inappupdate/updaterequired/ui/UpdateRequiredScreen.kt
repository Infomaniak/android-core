package com.infomaniak.core.inappupdate.updaterequired.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.infomaniak.core.inappupdate.R

@Composable
fun UpdateRequiredScreen(
    illustration: Painter,
    titleTextStyle: TextStyle,
    descriptionTextStyle: TextStyle,
    installUpdateButton: @Composable() () -> Unit,
) {
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center,
            ) { installUpdateButton() }
        },
    ) { scaffoldPadding ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        when (windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.COMPACT -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(scaffoldPadding)
                        .fillMaxSize(),
                ) {
                    Image(painter = illustration, contentDescription = null)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        style = titleTextStyle,
                        text = stringResource(R.string.updateAppTitle),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        style = descriptionTextStyle,
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.updateRequiredDescription)
                    )
                }
            }
            else -> {
                val rememberScrollState = rememberScrollState()

                Row(
                    modifier = Modifier.padding(scaffoldPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painter = illustration, contentDescription = null)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState)
                    ) {
                        Text(
                            style = titleTextStyle,
                            text = stringResource(R.string.updateAppTitle),
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            style = descriptionTextStyle,
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.updateRequiredDescription)
                        )
                    }
                }
            }
        }
    }
}
