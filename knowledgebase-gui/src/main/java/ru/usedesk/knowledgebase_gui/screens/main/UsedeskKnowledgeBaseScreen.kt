package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.CustomToolbar
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.rememberToolbarScrollBehavior
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.Event
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = (inflater.inflate(
        R.layout.usedesk_compose_screen,
        container,
        false
    ) as ComposeView).apply {
        val configuration =
            argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KNOWLEDGE_BASE_CONFIGURATION)
        UsedeskKnowledgeBaseSdk.init(requireContext(), configuration)
        setContent {
            val state by viewModel.modelFlow.collectAsState()
            ScreenRoot(state, viewModel::onEvent)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ScreenRoot(
        state: State,
        onEvent: (Event) -> Unit
    ) {
        val sectionsTitle = stringResource(R.string.usedesk_knowledgebase)
        val title = remember(state.currentScreen) {
            when (state.currentScreen) {
                else -> sectionsTitle
            }
        }

        val scrollBehavior = rememberToolbarScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CustomToolbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.usedesk_white_2)),
                        title = title,
                        scrollBehavior = scrollBehavior,
                        onBackPressed = requireActivity()::onBackPressed
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.usedesk_white_2))
                            .padding(
                                start = 16.dp,
                                end = 16.dp
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorResource(R.color.usedesk_gray_12))
                            .padding(
                                start = 8.dp,
                                end = 8.dp,
                                top = 6.dp,
                                bottom = 6.dp
                            )
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .align(Alignment.CenterVertically),
                            painter = painterResource(R.drawable.usedesk_ic_search),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                        BasicTextField(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            value = state.searchText,
                            onValueChange = remember { { onEvent(Event.SearchTextChanged(it)) } },
                            textStyle = TextStyle()
                        )
                    }
                }
            },
            content = {
                Content(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(colorResource(R.color.usedesk_white_2))
                )
            }
        )
    }

    @Composable
    private fun Content(
        state: State,
        onEvent: (Event) -> Unit,
        modifier: Modifier
    ) {
        //val navController = rememberNavController()

        Box(modifier = modifier) {
            when (state.currentScreen) {
                State.Screen.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
                is State.Screen.Sections -> {
                    ContentSections(
                        screen = state.currentScreen,
                        onEvent = onEvent
                    )
                }
                is State.Screen.Categories -> {
                    ContentCategories(
                        screen = state.currentScreen,
                        onEvent = onEvent
                    )
                }
                is State.Screen.Articles -> {
                    ContentArticles(
                        screen = state.currentScreen,
                        onEvent = onEvent
                    )
                }
                is State.Screen.Article -> {
                    ContentArticle(
                        screen = state.currentScreen,
                        onEvent = onEvent
                    )
                }
            }
        }
        /*NavHost(
            modifier = modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.usedesk_white_2)),
            navController = navController,
            startDestination = State.Screen.Sections::javaClass.name
        ) {
            composable(State.Screen.Sections::javaClass.name) {
                ScreenSections(
                    state = state,
                    onEvent = onEvent
                )
                /*
                { sectionId ->
                        navController.navigate(
                            route = State.Screen.Categories::javaClass.name
                        ) {
                            arguments = Bundle().apply {
                                putLong(SECTION_ID_KEY, sectionId)
                            }
                        }
                    }
                 */
            }
            composable(State.Screen.Categories::javaClass.name) {
                val sectionId = remember { it.arguments?.getLong(SECTION_ID_KEY) ?: 0L }
                ScreenCategories(
                    sectionId = sectionId,
                    state = state,
                    onEvent = onEvent
                )
                /*
                { categoryId ->
                        navController.navigate(
                            route = State.Screen.Articles::javaClass.name
                        ) {
                            arguments = Bundle().apply {
                                putLong(CATEGORY_ID_KEY, categoryId)
                            }
                        }
                    }
                 */
            }
            composable(State.Screen.Articles::javaClass.name) {
                val categoryId = remember { it.arguments?.getLong(CATEGORY_ID_KEY) ?: 0L }
                ScreenArticles(
                    categoryId = categoryId,
                    state = state,
                    onEvent = onEvent
                )
                /*{ articleId ->
                    navController.navigate(
                        route = State.Screen.Article::javaClass.name
                    ) {
                        arguments = Bundle().apply {
                            putLong(ARTICLE_ID_KEY, articleId)
                        }
                    }
                }*/
            }
        }*/
    }

    @Composable
    private fun LazyColumnCard(content: LazyListScope.() -> Unit) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(R.color.usedesk_white_1)),
            content = content
        )
    }

    @Composable
    private fun ContentSections(
        screen: State.Screen.Sections,
        onEvent: (Event) -> Unit
    ) {
        LazyColumnCard {
            items(
                items = screen.sections,
                key = UsedeskSection::id
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorResource(R.color.usedesk_white_1))
                        .clickableItem(
                            onClick = { onEvent(Event.SectionClicked(it)) } //TODO: try remember
                        )
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color = colorResource(R.color.usedesk_gray_cold_1))
                    ) {
                        BasicText(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = remember(it.title) {
                                it.title
                                    .firstOrNull(Char::isLetterOrDigit)
                                    ?.uppercase()
                                    ?: ""
                            },
                            style = TextStyle(
                                fontSize = 17.sp,
                                color = colorResource(R.color.usedesk_black_2)
                            )
                        )
                    }
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(
                                start = 10.dp,
                                end = 10.dp
                            )
                            .weight(weight = 1f, fill = true),
                        style = TextStyle(
                            fontSize = 17.sp,
                            textAlign = TextAlign.Start,
                            color = colorResource(R.color.usedesk_black_2)
                        ),
                        text = it.title
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(24.dp),
                        painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentCategories(
        screen: State.Screen.Categories,
        onEvent: (Event) -> Unit
    ) {
        LazyColumnCard {
            items(
                items = screen.section.categories,
                key = UsedeskCategory::id
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorResource(R.color.usedesk_white_1))
                        .clickableItem(
                            onClick = { onEvent(Event.CategoryClicked(it)) } //TODO: try remember
                        )
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(
                                start = 10.dp,
                                end = 10.dp
                            )
                            .weight(weight = 1f, fill = true),
                        style = TextStyle(
                            fontSize = 17.sp,
                            textAlign = TextAlign.Start,
                            color = colorResource(R.color.usedesk_black_2)
                        ),
                        text = it.title
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(24.dp),
                        painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentArticles(
        screen: State.Screen.Articles,
        onEvent: (Event) -> Unit
    ) {
        LazyColumnCard {
            items(
                items = screen.category.articles,
                key = UsedeskArticleInfo::id
            ) {
                BasicText(text = "Articles:${it.title}")
            }
        }
    }

    @Composable
    private fun ContentArticle(
        screen: State.Screen.Article,
        onEvent: (Event) -> Unit
    ) {
        LazyColumnCard { //TODO: тут поиск не нужен
            items(100) {
                BasicText(text = "Article:${screen.article.title}")
            }
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed()

    companion object {
        private const val SECTION_ID_KEY = "sectionsIdKey"
        private const val CATEGORY_ID_KEY = "categoryIdKey"
        private const val ARTICLE_ID_KEY = "articleIdKey"

        private const val WITH_SUPPORT_BUTTON_KEY = "withSupportButtonKey"
        private const val WITH_ARTICLE_RATING_KEY = "withArticleRatingKey"
        private const val KNOWLEDGE_BASE_CONFIGURATION = "knowledgeBaseConfiguration"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                withSupportButton,
                withArticleRating,
                knowledgeBaseConfiguration
            )
        }

        @JvmStatic
        @JvmOverloads
        fun createBundle( //TODO
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): Bundle = Bundle().apply {
            putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
            putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
            putParcelable(KNOWLEDGE_BASE_CONFIGURATION, knowledgeBaseConfiguration)
        }
    }
}