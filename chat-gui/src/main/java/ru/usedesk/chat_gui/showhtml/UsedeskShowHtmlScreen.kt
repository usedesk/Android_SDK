package ru.usedesk.chat_gui.showhtml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateBinding

class UsedeskShowHtmlScreen : UsedeskFragment(R.style.Usedesk_Theme_Chat) {
    private lateinit var rootView: ViewGroup
    private lateinit var wvContent: WebView
    private lateinit var ivClose: ImageView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        rootView = inflateBinding(inflater,
                container,
                R.layout.usedesk_screen_show_html,
                defaultStyleId)

        wvContent = rootView.findViewById(R.id.wv_content)
        ivClose = rootView.findViewById(R.id.iv_close)

        if (savedInstanceState == null) {
            wvContent.apply {
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

        ivClose.setOnClickListener {
            onBackPressed()
        }

        return rootView
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
}