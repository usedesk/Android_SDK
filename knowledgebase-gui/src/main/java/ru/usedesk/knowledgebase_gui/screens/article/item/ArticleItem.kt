package ru.usedesk.knowledgebase_gui.screens.article.item

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.*
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import kotlin.math.max
import kotlin.math.min

internal class ArticleItem : UsedeskFragment() {

    private lateinit var binding: Binding

    private val viewModel: ArticleItemViewModel by viewModels()

    private lateinit var messageStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var yesStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var noStyleValues: UsedeskResourceManager.StyleValues

    private var scrollY = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(layoutInflater,
                container,
                R.layout.usedesk_page_item_article_content,
                R.style.Usedesk_KnowledgeBase_Article_Content_Page_Item) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }.apply {
            messageStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_title_text)

            yesStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_yes_text)

            noStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_no_text)

            lRatingYes.visibility = View.GONE
            lRatingNo.visibility = View.GONE
            etRating.visibility = View.GONE
            tvRatingTitle.visibility = View.GONE

            val previousTitle = argsGetString(PREVIOUS_TITLE_KEY)
            if (previousTitle != null) {
                tvPrevious.text = previousTitle
                lPrevious.setOnClickListener {
                    parentFragment?.also {
                        if (it is IOnArticlePagesListener) {
                            it.onPrevious()
                        }
                    }
                }
            } else {
                lPrevious.visibility = View.INVISIBLE
            }

            val nextTitle = argsGetString(NEXT_TITLE_KEY)
            if (nextTitle != null) {
                tvNext.text = nextTitle
                lNext.setOnClickListener {
                    parentFragment?.also {
                        if (it is IOnArticlePagesListener) {
                            it.onNext()
                        }
                    }
                }
            } else {
                lNext.visibility = View.INVISIBLE
            }

            lContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                this@ArticleItem.scrollY = scrollY
                updateFab()
            })

            lContent.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                lContentScrollable.minimumHeight = rootView.height - lBottomNavigation.measuredHeight
                updateFab()
            }
        }

        argsGetLong(ARTICLE_ID_KEY)?.also { articleId ->
            viewModel.init(articleId)
        }

        viewModel.articleContentLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.state) {
                    ArticleItemViewModel.ArticleContentState.State.LOADING -> {
                        showInstead(binding.pbLoading, binding.lContent, gone = false)
                    }
                    ArticleItemViewModel.ArticleContentState.State.LOADED -> {
                        showInstead(binding.lContent, binding.pbLoading, gone = false)
                        it.articleContent?.apply {
                            onArticleContent(this)
                        }
                    }
                    else -> {

                    }
                }
            }
        }

        return binding.rootView
    }

    private fun updateFab() {
        binding.apply {
            val dif = max(lContentScrollable.height, lContentScrollable.minimumHeight) - (scrollY + lContent.height)
            btnSupport.y = (rootView.height - btnSupport.height - btnSupport.marginBottom + min(0, dif)).toFloat()
        }
    }

    private fun onArticleContent(articleContent: UsedeskArticleContent) {
        updateFab()
        binding.wvContent.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    showQuestion(articleContent.id)
                }
            }
            loadData(articleContent.text, "text/html", null)
            setBackgroundColor(Color.TRANSPARENT)
            showInstead(this, binding.pbLoading, gone = false)
        }
    }

    private fun showQuestion(articleId: Long) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingNo.text = noStyleValues.getString(R.attr.usedesk_text_1)

        binding.tvRatingYes.setOnClickListener {
            viewModel.sendArticleRating(articleId, true)

            showThanks()
        }
        binding.tvRatingNo.setOnClickListener {
            viewModel.sendArticleRating(articleId, false)

            showWhatsWrong(articleId)
        }

        binding.tvRatingTitle.visibility = View.VISIBLE
        binding.lRatingYes.visibility = View.VISIBLE
        binding.lRatingNo.visibility = View.VISIBLE
        binding.etRating.visibility = View.GONE
    }

    private fun showWhatsWrong(articleId: Long) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_2)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_2)

        binding.tvRatingYes.setOnClickListener {
            viewModel.sendArticleRating(articleId, binding.etRating.text.toString())

            showThanks()
        }

        binding.lRatingNo.visibility = View.GONE
        binding.tvRatingTitle.visibility = View.VISIBLE
        binding.lRatingYes.visibility = View.VISIBLE
        binding.etRating.visibility = View.VISIBLE
    }

    private fun showThanks() {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_3)
        binding.lRatingYes.visibility = View.GONE
        binding.lRatingNo.visibility = View.GONE
        binding.etRating.visibility = View.GONE
        binding.tvRatingTitle.visibility = View.VISIBLE
    }

    companion object {
        private const val ARTICLE_ID_KEY = "articleIdKey"
        private const val PREVIOUS_TITLE_KEY = "previousTitleKey"
        private const val NEXT_TITLE_KEY = "nextTitleKey"

        fun newInstance(articleId: Long,
                        previousTitle: String?,
                        nextTitle: String?): ArticleItem {
            return ArticleItem().apply {
                arguments = Bundle().apply {
                    putLong(ARTICLE_ID_KEY, articleId)
                    putString(PREVIOUS_TITLE_KEY, previousTitle)
                    putString(NEXT_TITLE_KEY, nextTitle)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val lContent: NestedScrollView = rootView.findViewById(R.id.l_content)
        val lContentScrollable: View = rootView.findViewById(R.id.l_content_scrollable)
        val lBottomNavigation: View = rootView.findViewById(R.id.l_bottom_navigation)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val tvRatingTitle: TextView = rootView.findViewById(R.id.tv_rating_title)
        val etRating: EditText = rootView.findViewById(R.id.et_rating_message)
        val lRatingYes: View = rootView.findViewById(R.id.l_rating_yes)
        val tvRatingYes: TextView = rootView.findViewById(R.id.tv_rating_yes)
        val lRatingNo: View = rootView.findViewById(R.id.l_rating_no)
        val tvRatingNo: TextView = rootView.findViewById(R.id.tv_rating_no)
        val lPrevious: View = rootView.findViewById(R.id.l_previous)
        val tvPrevious: TextView = rootView.findViewById(R.id.tv_previous)
        val lNext: View = rootView.findViewById(R.id.l_next)
        val tvNext: TextView = rootView.findViewById(R.id.tv_next)
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
    }
}
