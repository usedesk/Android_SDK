package ru.usedesk.chat_gui.chat

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskToolbarAdapter
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_sdk.UsedeskLog

class UsedeskChatScreen : UsedeskFragment() {

    private val viewModel: ChatViewModel by viewModels(
        ownerProducer = {
            findChatViewModelStoreOwner() ?: this
        }
    )
    private val playerViewModel: PlayerViewModel by viewModels(
        ownerProducer = {
            findChatViewModelStoreOwner() ?: this
        })

    internal val mediaPlayerAdapter: MediaPlayerAdapter by lazy {
        MediaPlayerAdapter(
            this,
            playerViewModel
        )
    }

    private lateinit var binding: Binding
    private lateinit var toolbarAdapter: UsedeskToolbarAdapter
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onCreateView")
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_screen_chat,
            R.style.Usedesk_Chat_Screen
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        navHostFragment =
            childFragmentManager.findFragmentById(R.id.page_container) as NavHostFragment
        navController = navHostFragment.navController

        toolbarAdapter = UsedeskToolbarAdapter(binding.toolbar).apply {
            setBackButton {
                requireActivity().onBackPressed()
            }
        }

        val agentName = argsGetString(AGENT_NAME_KEY)
        val rejectedFileExtensions = argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY, arrayOf())
        argsGetParcelable<UsedeskChatConfiguration>(CHAT_CONFIGURATION_KEY)?.let {
            UsedeskChatSdk.setConfiguration(it)
        }

        init(agentName, rejectedFileExtensions)

        return binding.rootView
    }

    private fun init(
        agentName: String?,
        rejectedFileExtensions: Array<String>
    ) {
        UsedeskChatSdk.init(requireContext())

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = when (destination.id) {
                R.id.dest_loading_page,
                R.id.dest_messages_page -> {
                    binding.styleValues
                        .getStyleValues(R.attr.usedesk_common_toolbar)
                        .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                        .getString(android.R.attr.text)
                }
                R.id.dest_offline_form_page -> {
                    viewModel.modelLiveData.value.offlineFormSettings?.callbackTitle
                }
                R.id.dest_offline_form_selector_page -> {
                    viewModel.modelLiveData.value.offlineFormSettings?.topicsTitle
                }
                else -> null
            }
            toolbarAdapter.setTitle(title)
        }
        viewModel.modelLiveData.initAndObserveWithOld(viewLifecycleOwner) { old, new ->
            if (old != null &&
                new.clientToken != null &&
                old.clientToken != new.clientToken
            ) {
                findParent<IUsedeskOnClientTokenListener>()?.onClientToken(new.clientToken)
            }
            if (old?.offlineFormSettings != new.offlineFormSettings) {
                updateTitle(navController.currentDestination)
            }
        }

        viewModel.init(agentName, rejectedFileExtensions.toSet())
    }

    private fun updateTitle(destination: NavDestination?) {
        val title = when (destination?.id) {
            R.id.dest_loading_page,
            R.id.dest_messages_page -> {
                binding.styleValues
                    .getStyleValues(R.attr.usedesk_common_toolbar)
                    .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                    .getString(android.R.attr.text)
            }
            R.id.dest_offline_form_page -> {
                viewModel.modelLiveData.value.offlineFormSettings?.callbackTitle
            }
            R.id.dest_offline_form_selector_page -> {
                viewModel.modelLiveData.value.offlineFormSettings?.topicsTitle
            }
            else -> null
        }
        toolbarAdapter.setTitle(title)
    }

    override fun onStart() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onStart")
        super.onStart()

        UsedeskChatSdk.stopService(requireContext())
    }

    override fun onStop() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onStop")
        super.onStop()
        UsedeskChatSdk.startService(requireContext())
    }

    override fun onPause() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onPause")
        super.onPause()
    }

    override fun onResume() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onResume")
        super.onResume()
    }

    override fun onDestroy() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onDetach")
        super.onDetach()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onAttachFragment")
        super.onAttachFragment(childFragment)
    }

    override fun onAttach(context: Context) {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onAttach context")
        super.onAttach(context)
    }

    override fun onAttach(activity: Activity) {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onAttach activity")
        super.onAttach(activity)
    }

    override fun onDestroyView() {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onDestroyView")
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        UsedeskLog.onLog(Integer.toHexString(System.identityHashCode(this)), "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onBackPressed(): Boolean {
        return mediaPlayerAdapter.onBackPressed() || navController.popBackStack()
    }

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"
        private const val REJECTED_FILE_EXTENSIONS_KEY = "rejectedFileExtensionsKey"
        private const val CHAT_CONFIGURATION_KEY = "chatConfigurationKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            usedeskChatConfiguration: UsedeskChatConfiguration? = null
        ): UsedeskChatScreen {
            return UsedeskChatScreen().apply {
                arguments = createBundle(
                    agentName,
                    rejectedFileExtensions,
                    usedeskChatConfiguration
                )
            }
        }

        @JvmOverloads
        @JvmStatic
        fun createBundle(
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            usedeskChatConfiguration: UsedeskChatConfiguration? = null
        ): Bundle {
            return Bundle().apply {
                if (agentName != null) {
                    putString(AGENT_NAME_KEY, agentName)
                }
                val extensions = rejectedFileExtensions?.map {
                    '.' + it.trim(' ', '.')
                }?.toTypedArray() ?: arrayOf()
                if (usedeskChatConfiguration != null) {
                    putParcelable(CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
                }
                putStringArray(REJECTED_FILE_EXTENSIONS_KEY, extensions)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)

    }
}