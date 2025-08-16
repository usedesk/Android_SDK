
package ru.usedesk.chat_gui.chat.offlineformselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.insetsAsPaddings

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
        inflater = inflater,
        container = container,
        defaultLayoutId = R.layout.usedesk_page_offline_form_selector,
        defaultStyleId = R.style.Usedesk_Chat_Screen_Offline_Form_Selector_Page,
        createBinding = ::Binding
    ).apply {
        binding = this

        findParent<UsedeskChatScreen>()?.run {
            val chatArgs = getChatArgs(savedInstanceState)
            if (chatArgs.supportWindowInsets) {
                binding.rvItems.insetsAsPaddings(ignoreStatusBar = true)
            }
        }

        val key = argsGetString(KEY_KEY, "")
        OfflineFormSelectorAdapter(
            key = key,
            recyclerView = rvItems,
            binding = binding,
            viewModel = viewModel,
            lifecycleCoroutineScope = lifecycleScope
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