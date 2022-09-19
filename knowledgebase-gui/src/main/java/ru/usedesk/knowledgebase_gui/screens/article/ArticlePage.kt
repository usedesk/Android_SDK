package ru.usedesk.knowledgebase_gui.screens.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.article.item.IOnArticlePagesListener
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen

internal class ArticlePage : UsedeskFragment(), IOnArticlePagesListener {

    private val viewModel: ArticlePageViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var articlePagesAdapter: ArticlePagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_article_content,
            R.style.Usedesk_KnowledgeBase_Article_Content_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        argsGetLong(CATEGORY_ID_KEY)?.let { categoryId ->
            argsGetLong(ARTICLE_ID_KEY)?.let { articleId ->
                init(categoryId, articleId)
            }
        }

        return binding.rootView
    }

    fun init(categoryId: Long, articleId: Long) {
        val withArticleRating = findParent<UsedeskKnowledgeBaseScreen>()
            ?.withArticleRating ?: true
        articlePagesAdapter = ArticlePagesAdapter(
            binding.vpPages,
            childFragmentManager,
            viewModel,
            lifecycleScope,
            withArticleRating
        )

        viewModel.init(categoryId, articleId)

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.selectedArticle != new.selectedArticle && new.selectedArticle != null) {
                findParent<UsedeskKnowledgeBaseScreen>()?.onTitle(new.selectedArticle.title)
            }
        }
    }

    override fun onPrevious() {
        articlePagesAdapter.onPrevious()
    }

    override fun onNext() {
        articlePagesAdapter.onNext()
    }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"
        private const val ARTICLE_ID_KEY = "articleIdKey"

        fun createBundle(
            title: String,
            categoryId: Long,
            articleId: Long
        ): Bundle {
            return Bundle().apply {
                putString(UsedeskKnowledgeBaseScreen.COMMON_TITLE_KEY, title)
                putLong(CATEGORY_ID_KEY, categoryId)
                putLong(ARTICLE_ID_KEY, articleId)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val vpPages: ViewPager = rootView.findViewById(R.id.vp_pages)
    }
}