package ru.usedesk.knowledgebase_gui.screens.main.article

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import ru.usedesk.knowledgebase_gui.compose.LazyColumnCard
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State

@Composable
internal fun ContentArticle(
    screen: State.Screen.Article,
    onEvent: (Event) -> Unit
) {
    LazyColumnCard { //TODO: тут поиск не нужен
        items(100) {
            BasicText(text = "Article:${screen.articleId}")
        }
    }
}