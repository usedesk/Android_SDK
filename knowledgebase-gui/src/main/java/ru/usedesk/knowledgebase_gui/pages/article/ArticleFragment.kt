package ru.usedesk.knowledgebase_gui.pages.article

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showInstead
import ru.usedesk.knowledgebase_gui.R

internal class ArticleFragment : UsedeskFragment() {

    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_fragment_article,
                    R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            val articleId = argsGetLong(ARTICLE_ID_KEY)
            if (articleId != null) {
                init(articleId)
            }
        }

        return binding.rootView
    }

    private fun init(articleId: Long) {
        viewModel.init(articleId)

        showInstead(binding.pbLoading, binding.wvContent)

        viewModel.articleLiveData.observe(viewLifecycleOwner) {
            binding.tvTitle.text = it.title
            binding.wvContent.loadData(it.text, "text/html", null)
            binding.wvContent.setBackgroundColor(Color.TRANSPARENT)
            showInstead(binding.wvContent, binding.pbLoading)
        }
    }

    companion object {
        private const val ARTICLE_ID_KEY = "articleIdKey"

        fun newInstance(articleId: Long): ArticleFragment {
            return ArticleFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARTICLE_ID_KEY, articleId)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
    }
}