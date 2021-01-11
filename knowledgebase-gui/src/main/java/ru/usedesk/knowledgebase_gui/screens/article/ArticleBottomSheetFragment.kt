package ru.usedesk.knowledgebase_gui.screens.article

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.usedesk.common_gui.*
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleBottomSheetFragment private constructor(
        container: ViewGroup,
        dialogStyle: Int
) : UsedeskBottomSheetDialog(container.context, dialogStyle) {

    private val binding: Binding

    init {
        binding = inflateItem(layoutInflater,
                container,
                R.layout.usedesk_page_article_content,
                dialogStyle) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }.apply {
            setContentView(rootView)
        }

        binding.ivClose.setOnClickListener {
            this.hide()
        }
    }

    override fun onStart() {
        super.onStart()

        this.findViewById<View>(R.id.design_bottom_sheet)?.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            BottomSheetBehavior.from(this).apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    fun onArticleContent(articleContent: UsedeskArticleContent) {
        binding.tvTitle.text = articleContent.title
        binding.wvContent.loadData(articleContent.text, "text/html", null)
        binding.wvContent.setBackgroundColor(Color.TRANSPARENT)
        showInstead(binding.wvContent, binding.pbLoading)
    }

    fun onLoading() {
        showInstead(binding.pbLoading, binding.wvContent)
    }

    companion object {
        fun newInstance(container: View): ArticleBottomSheetFragment {
            val dialogStyle = UsedeskResourceManager.getResourceId(R.style.Usedesk_KnowledgeBase_Article_Content_Dialog)
            return ArticleBottomSheetFragment(container as ViewGroup, dialogStyle)
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val ivClose: ImageView = rootView.findViewById(R.id.iv_close)
    }
}
