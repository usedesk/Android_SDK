package ru.usedesk.chat_gui.internal.showhtml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.ScreenShowHtmlBinding
import ru.usedesk.chat_gui.internal._extra.UsedeskFragment
import ru.usedesk.common_gui.internal.inflateItem


class ShowHtmlScreen : UsedeskFragment() {
    private lateinit var binding: ScreenShowHtmlBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = inflateItem(inflater,
                R.layout.screen_show_html,
                container)

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

        return binding.root
    }

    companion object {
        private const val HTML_TEXT_KEY = "htmlTextKey"

        fun newInstance(htmlText: String): ShowHtmlScreen {
            val args = Bundle()
            args.putString(HTML_TEXT_KEY, htmlText)
            val fragment = ShowHtmlScreen()
            fragment.arguments = args
            return fragment
        }
    }
}