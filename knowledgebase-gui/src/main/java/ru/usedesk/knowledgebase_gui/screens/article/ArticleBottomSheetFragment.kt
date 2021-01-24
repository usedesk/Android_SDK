package ru.usedesk.knowledgebase_gui.screens.article

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
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

    private val messageStyleValues: UsedeskResourceManager.StyleValues
    private val yesStyleValues: UsedeskResourceManager.StyleValues
    private val noStyleValues: UsedeskResourceManager.StyleValues

    init {
        binding = inflateItem(layoutInflater,
                container,
                R.layout.usedesk_page_article_content,
                dialogStyle) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }.apply {
            setContentView(rootView)

            messageStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_title_text)

            yesStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_yes_text)

            noStyleValues = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_no_text)

            ivClose.setOnClickListener {
                dismiss()
            }

            lRatingYes.visibility = View.GONE
            lRatingNo.visibility = View.GONE
            etRating.visibility = View.GONE
            tvRatingTitle.visibility = View.GONE
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

    fun onArticleContent(articleContent: UsedeskArticleContent,
                         onRating: (Long, Boolean) -> Unit,
                         onRatingMessage: (Long, String) -> Unit) {
        binding.tvTitle.text = articleContent.title
        binding.wvContent.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    showQuestion(articleContent.id, onRating, onRatingMessage)
                }
            }
            loadData(articleContent.text, "text/html", null)
            setBackgroundColor(Color.TRANSPARENT)
            showInstead(this, binding.pbLoading)
        }
    }

    private fun showQuestion(articleId: Long,
                             onRating: (Long, Boolean) -> Unit,
                             onRatingMessage: (Long, String) -> Unit) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingNo.text = noStyleValues.getString(R.attr.usedesk_text_1)

        binding.tvRatingYes.setOnClickListener {
            onRating(articleId, true)

            showThanks()
        }
        binding.tvRatingNo.setOnClickListener {
            onRating(articleId, false)

            showWhatsWrong(articleId, onRatingMessage)
        }

        binding.tvRatingTitle.visibility = View.VISIBLE
        binding.lRatingYes.visibility = View.VISIBLE
        binding.lRatingNo.visibility = View.VISIBLE
        binding.etRating.visibility = View.GONE
    }

    private fun showWhatsWrong(articleId: Long,
                               onRatingMessage: (Long, String) -> Unit) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_2)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_2)

        binding.tvRatingYes.setOnClickListener {
            onRatingMessage(articleId, binding.etRating.text.toString())

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

    fun onLoading() {
        showInstead(binding.pbLoading, binding.wvContent)
    }

    companion object {
        fun newInstance(container: View): ArticleBottomSheetFragment {
            val dialogStyle = UsedeskResourceManager.getResourceId(R.style.Usedesk_KnowledgeBase_Article_Content_Page)
            return ArticleBottomSheetFragment(container as ViewGroup, dialogStyle)
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val ivClose: ImageView = rootView.findViewById(R.id.iv_close)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val tvRatingTitle: TextView = rootView.findViewById(R.id.tv_rating_title)
        val etRating: EditText = rootView.findViewById(R.id.et_rating_message)
        val lRatingYes: View = rootView.findViewById(R.id.l_rating_yes)
        val tvRatingYes: TextView = rootView.findViewById(R.id.tv_rating_yes)
        val lRatingNo: View = rootView.findViewById(R.id.l_rating_no)
        val tvRatingNo: TextView = rootView.findViewById(R.id.tv_rating_no)
    }
}
