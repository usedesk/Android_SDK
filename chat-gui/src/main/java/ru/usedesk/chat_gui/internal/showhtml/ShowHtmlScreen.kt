package ru.usedesk.chat_gui.internal.showhtml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.internal._extra.UsedeskFragment
import ru.usedesk.common_gui.internal.argsGetInt
import ru.usedesk.common_gui.internal.inflateFragment

class ShowHtmlScreen : UsedeskFragment() {
    private lateinit var rootView: ViewGroup
    private lateinit var wvContent: WebView
    private lateinit var ivClose: ImageView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val themeId = argsGetInt(arguments, THEME_ID_KEY, R.style.Usedesk_Theme_Chat)

        rootView = inflateFragment(inflater, container, themeId, R.layout.usedesk_screen_show_html)

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
        private const val THEME_ID_KEY = "themeIdKey"

        @JvmOverloads
        fun newInstance(themeId: Int? = null, htmlText: String): ShowHtmlScreen {
            return ShowHtmlScreen().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                    putString(HTML_TEXT_KEY, htmlText)
                }
            }
        }
    }
}