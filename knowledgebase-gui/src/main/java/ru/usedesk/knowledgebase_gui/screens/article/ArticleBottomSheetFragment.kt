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
            dismiss()
        }

        binding.lFeedbackYes.visibility = View.GONE
        binding.lFeedbackNo.visibility = View.GONE
        binding.etFeedback.visibility = View.GONE
        binding.tvFeedbackTitle.visibility = View.GONE
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
                         onFeedback: (Long, Boolean) -> Unit,
                         onFeedbackMessage: (Long, String) -> Unit) {
        binding.tvTitle.text = articleContent.title
        binding.wvContent.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    showQuestion(articleContent.id, onFeedback, onFeedbackMessage)
                }
            }
            loadData(articleContent.text, "text/html", null)
            setBackgroundColor(Color.TRANSPARENT)
            showInstead(this, binding.pbLoading)
        }
    }

    private fun showQuestion(articleId: Long,
                             onFeedback: (Long, Boolean) -> Unit,
                             onFeedbackMessage: (Long, String) -> Unit) {
        binding.tvFeedbackTitle.setText(R.string.article_feedback_question)
        binding.tvFeedbackYes.setText(R.string.article_feedback_yes)
        binding.tvFeedbackNo.setText(R.string.article_feedback_no)

        binding.tvFeedbackYes.setOnClickListener {
            onFeedback(articleId, true)

            showThanks()
        }
        binding.tvFeedbackNo.setOnClickListener {
            onFeedback(articleId, false)

            showWhatsWrong(articleId, onFeedbackMessage)
        }

        binding.tvFeedbackTitle.visibility = View.VISIBLE
        binding.lFeedbackYes.visibility = View.VISIBLE
        binding.lFeedbackNo.visibility = View.VISIBLE
        binding.etFeedback.visibility = View.GONE
    }

    private fun showWhatsWrong(articleId: Long,
                               onFeedbackMessage: (Long, String) -> Unit) {
        binding.tvFeedbackTitle.setText(R.string.article_feedback_whats_wrong)
        binding.tvFeedbackYes.setText(R.string.article_feedback_send)

        binding.tvFeedbackYes.setOnClickListener {
            onFeedbackMessage(articleId, binding.etFeedback.text.toString())

            showThanks()
        }

        binding.lFeedbackNo.visibility = View.GONE
        binding.tvFeedbackTitle.visibility = View.VISIBLE
        binding.lFeedbackYes.visibility = View.VISIBLE
        binding.etFeedback.visibility = View.VISIBLE
    }

    private fun showThanks() {
        binding.tvFeedbackTitle.setText(R.string.article_feedback_thanks)
        binding.lFeedbackYes.visibility = View.GONE
        binding.lFeedbackNo.visibility = View.GONE
        binding.etFeedback.visibility = View.GONE
        binding.tvFeedbackTitle.visibility = View.VISIBLE
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
        val ivClose: ImageView = rootView.findViewById(R.id.iv_close)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val tvFeedbackTitle: TextView = rootView.findViewById(R.id.tv_feedback_title)
        val etFeedback: EditText = rootView.findViewById(R.id.et_feedback_message)
        val lFeedbackYes: View = rootView.findViewById(R.id.l_feedback_yes)
        val tvFeedbackYes: TextView = rootView.findViewById(R.id.tv_feedback_yes)
        val lFeedbackNo: View = rootView.findViewById(R.id.l_feedback_no)
        val tvFeedbackNo: TextView = rootView.findViewById(R.id.tv_feedback_no)
    }
}
