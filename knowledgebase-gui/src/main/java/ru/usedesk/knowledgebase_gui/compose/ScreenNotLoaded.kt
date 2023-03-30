package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization

@Composable
internal fun ScreenNotLoaded(
    customization: UsedeskKnowledgeBaseCustomization,
    tryAgain: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                painter = painterResource(R.drawable.usedesk_image_cant_load),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = 24.dp,
                    end = 24.dp
                )
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                text = stringResource(customization.textIdSearchLoadError),
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = colorResource(customization.colorIdBlack2),
                    textAlign = TextAlign.Center
                )
            )
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                visible = tryAgain != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableText(onClick = tryAgain ?: remember { {} })
                        .align(Alignment.CenterHorizontally),
                    text = stringResource(customization.textIdSearchTryAgain),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = colorResource(customization.colorIdBlue),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}