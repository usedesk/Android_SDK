package ru.usedesk.knowledgebase_gui.screens.main.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State

@Composable
internal fun ContentLoading(
    screen: State.Screen.Loading,
    onEvent: (Event) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.Center)
        )
    }
}