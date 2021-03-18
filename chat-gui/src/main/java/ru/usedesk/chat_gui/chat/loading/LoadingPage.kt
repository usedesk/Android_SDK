package ru.usedesk.chat_gui.chat.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class LoadingPage : UsedeskFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = inflateItem(inflater,
                container,
                R.layout.usedesk_page_loading,
                R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        return binding.rootView
    }

    companion object {
        fun newInstance(): LoadingPage {
            return LoadingPage()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvLoading: TextView = rootView.findViewById(R.id.tv_loading)
    }
}