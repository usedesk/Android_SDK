package ru.usedesk.knowledgebase_gui.screen.compose.article

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.MutableContextWrapper
import android.os.Handler
import android.os.Looper
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
import ru.usedesk.common_gui.UsedeskOnFullscreenListener
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

// We deliberately hold Views in this ViewModel: the WebView's DOM (and the rating ComposeView's
// composition) must outlive an Activity recreate. Views are constructed with a MutableContextWrapper
// whose baseContext is swapped back to the Application on detach(), so no Activity reference leaks.
@SuppressLint("StaticFieldLeak")
internal class ArticleViewsHolder(application: Application) : AndroidViewModel(application) {

    private val wrapper = MutableContextWrapper(application)

    val webView: ArticleWebView = ArticleWebView(wrapper)

    private val mainHandler = Handler(Looper.getMainLooper())
    private val resumeFullscreen = Runnable {
        if (!webView.isInFullscreen) return@Runnable
        val listener = webView.fullscreenListener
        val container = listener?.getFullscreenLayout()
        when (container) {
            null -> webView.exitFullscreen()
            else -> {
                webView.reattachCustomView(container)
                listener.onFullscreenChanged(true)
            }
        }
    }

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

    fun attachTo(activity: Activity, fullscreenListener: UsedeskOnFullscreenListener?) {
        wrapper.baseContext = activity
        webView.fullscreenListener = fullscreenListener
        if (webView.isInFullscreen) {
            // Fragment.onAttach fires inside the host's super.onCreate — its lateinit binding (and
            // thus getFullscreenLayout) isn't ready yet. Defer the FS reattach to the next looper tick.
            mainHandler.removeCallbacks(resumeFullscreen)
            mainHandler.post(resumeFullscreen)
        }
    }

    fun detach() {
        mainHandler.removeCallbacks(resumeFullscreen)
        // The custom view belongs to the leaving Activity's window. Detach it but keep fullscreen
        // state — attachTo will reparent the same view onto the new Activity's container.
        if (webView.isInFullscreen) {
            webView.detachCustomView()
        }
        webView.fullscreenListener = null
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
        mainHandler.removeCallbacks(resumeFullscreen)
        // Fragment is going away for good — actually exit fullscreen (callback + signal listener).
        webView.exitFullscreen()
        detach()
        (webView.parent as? ViewGroup)?.removeView(webView)
        (ratingView.parent as? ViewGroup)?.removeView(ratingView)
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
