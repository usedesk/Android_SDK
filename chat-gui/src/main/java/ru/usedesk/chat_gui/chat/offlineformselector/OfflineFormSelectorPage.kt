
package ru.usedesk.chat_gui.chat.offlineformselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormSelectorPage : UsedeskFragment() {

    private val viewModel: OfflineFormViewModel by viewModels(
        ownerProducer = this@OfflineFormSelectorPage::requireChatViewModelStoreOwner
    )

    private lateinit var binding: Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflateItem(
        inflater,
        container,
        R.layout.usedesk_page_offline_form_selector,
        R.style.Usedesk_Chat_Screen_Offline_Form_Selector_Page,
        ::Binding
    ).apply {
        binding = this

        val key = argsGetString(KEY_KEY, "")
        OfflineFormSelectorAdapter(
            key,
            rvItems,
            binding,
            viewModel,
            lifecycleScope
        )
    }.rootView

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
    }

    companion object {
        private const val KEY_KEY = "a"

        fun createBundle(key: String) = Bundle().apply {
            putString(KEY_KEY, key)
        }
    }
}