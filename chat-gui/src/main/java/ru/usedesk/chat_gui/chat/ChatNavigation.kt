package ru.usedesk.chat_gui.chat

import android.view.View
import androidx.fragment.app.FragmentManager
import ru.usedesk.chat_gui.chat.loading.LoadingPage
import ru.usedesk.chat_gui.chat.messages.MessagesPage
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormPage
import ru.usedesk.chat_gui.chat.offlineformselector.OfflineFormSelectorPage
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.hideKeyboard

class ChatNavigation(
        private val fragmentManager: FragmentManager,
        private val rootView: View,
        private val containerId: Int
) {
    fun goLoading() {
        navigateToRoot(LoadingPage.newInstance())
    }

    fun goMessages(agentName: String?) {
        navigateToRoot(MessagesPage.newInstance(agentName))
    }

    fun goOfflineForm() {
        navigateToRoot(OfflineFormPage.newInstance())
    }

    fun goOfflineFormSelector(items: Array<String>, selectedIndex: Int) {
        navigateForwardTo(OfflineFormSelectorPage.newInstance(items, selectedIndex))
    }

    private fun navigateToRoot(vararg fragments: UsedeskFragment) {
        rootView.post {
            fragmentManager.beginTransaction().also { transaction ->
                fragmentManager.fragments.reversed()
                        .forEach {
                            transaction.remove(it)
                        }
                fragments.forEach { fragment ->
                    transaction.add(containerId, fragment)
                }
                fragments.lastOrNull()?.let {
                    transaction.show(it)
                    it.view?.run {
                        hideKeyboard(this)
                    }
                }
            }.commit()
        }
    }

    private fun navigateForwardTo(vararg fragments: UsedeskFragment) {
        fragmentManager.beginTransaction().also { transaction ->
            fragments.forEach { fragment ->
                transaction.add(containerId, fragment)
            }
            fragmentManager.fragments.lastOrNull()?.let {
                transaction.hide(it)
                it.view?.run {
                    hideKeyboard(this)
                }
            }
            fragments.lastOrNull()?.let {
                transaction.show(it)
                it.view?.run {
                    hideKeyboard(this)
                }
            }
        }.commit()
    }

    fun onBackPressed(): Boolean {
        return if (fragmentManager.fragments.count() < 2) {
            false
        } else {
            fragmentManager.beginTransaction().also { transaction ->
                val current = fragmentManager.fragments[fragmentManager.fragments.size - 1]
                transaction.remove(current)
                val last = fragmentManager.fragments[fragmentManager.fragments.size - 2]
                transaction.show(last)
            }.commit()
            true
        }
    }
}