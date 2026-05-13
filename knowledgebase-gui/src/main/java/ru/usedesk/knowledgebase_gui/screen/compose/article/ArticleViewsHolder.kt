package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.app.Activity
import android.app.Application
import android.content.MutableContextWrapper
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.AndroidViewModel
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

internal class ArticleViewsHolder(application: Application) : AndroidViewModel(application) {

    // Views outlive any single Activity; swap to the live one per attach for window-bound ops
    private val wrapper = MutableContextWrapper(application)

    val webView: ArticleWebView = ArticleWebView(wrapper)

    private var ratingBinding by mutableStateOf<RatingBinding?>(null)

    val ratingView: ComposeView = ComposeView(wrapper).apply {
        setContent {
            val binding = ratingBinding ?: return@setContent
            val state by binding.viewModel.modelFlow.collectAsState()
            AnimatedVisibility(
                visible = state.articleShowed,
                enter = remember(binding.theme) { fadeIn(binding.theme.animationSpec()) },
                exit = remember(binding.theme) { fadeOut(binding.theme.animationSpec()) }
            ) {
                ArticleRating(
                    theme = binding.theme,
                    state = state,
                    onReviewGoodClick = binding.onReviewGoodClick,
                    onReviewBadClick = binding.onReviewBadClick
                )
            }
        }
    }

    fun attachTo(activity: Activity) {
        wrapper.baseContext = activity
    }

    fun detach() {
        wrapper.baseContext = getApplication()
    }

    fun bindRating(
        theme: UsedeskKnowledgeBaseTheme,
        viewModel: ArticleViewModel,
        onReviewGoodClick: () -> Unit,
        onReviewBadClick: () -> Unit
    ) {
        ratingBinding = RatingBinding(theme, viewModel, onReviewGoodClick, onReviewBadClick)
    }

    fun unbindRating(viewModel: ArticleViewModel) {
        if (ratingBinding?.viewModel === viewModel) ratingBinding = null
    }

    override fun onCleared() {
        (webView.parent as? ViewGroup)?.removeView(webView)
        (ratingView.parent as? ViewGroup)?.removeView(ratingView)
        detach()
        webView.destroy()
        ratingView.disposeComposition()
    }

    private data class RatingBinding(
        val theme: UsedeskKnowledgeBaseTheme,
        val viewModel: ArticleViewModel,
        val onReviewGoodClick: () -> Unit,
        val onReviewBadClick: () -> Unit
    )
}
