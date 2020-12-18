package ru.usedesk.chat_gui.showhtml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

class UsedeskShowHtmlScreen : UsedeskFragment() {
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_screen_show_html,
                R.style.Usedesk_Chat_Show_Html) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        if (savedInstanceState == null) {
            binding.wvContent.apply {
                val htmlText = arguments?.getString(HTML_TEXT_KEY, "")

                loadDataWithBaseURL(null,
                        htmlText,
                        "text/html",
                        null,
                        null)

                settings.apply {
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
            }
        }

        binding.ivClose.setOnClickListener {
            onBackPressed()
        }

        return binding.rootView
    }

    companion object {
        private const val HTML_TEXT_KEY = "htmlTextKey"

        @JvmStatic
        fun newInstance(htmlText: String): UsedeskShowHtmlScreen {
            return UsedeskShowHtmlScreen().apply {
                arguments = Bundle().apply {
                    putString(HTML_TEXT_KEY, htmlText)
                }
            }
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val ivClose: ImageView = rootView.findViewById(R.id.iv_close)
    }
}