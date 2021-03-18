package ru.usedesk.chat_gui.chat

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import ru.usedesk.chat_gui.chat.loading.LoadingPage
import ru.usedesk.chat_gui.chat.messages.MessagesPage
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormPage
import ru.usedesk.chat_gui.chat.offlineformselector.OfflineFormSelectorPage
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.hideKeyboard

internal class ChatNavigation(
        private val fragmentManager: FragmentManager,
        private val rootView: View,
        private val containerId: Int
) {
    private val pageSubject = BehaviorSubject.create<Page>()

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

    private fun navigateToRoot(fragment: UsedeskFragment) {
        rootView.post {
            fragmentManager.beginTransaction().also { transaction ->
                fragmentManager.fragments.reversed()
                        .forEach {
                            transaction.remove(it)
                        }
                transaction.add(containerId, fragment)
                transaction.show(fragment)
                fragment.view?.run {
                    hideKeyboard(this)
                }
                onFragmentChanged(fragment)
            }.commit()
        }
    }

    private fun navigateForwardTo(fragment: UsedeskFragment) {
        fragmentManager.beginTransaction().also { transaction ->
            fragmentManager.fragments.lastOrNull()?.let {
                transaction.hide(it)
                it.view?.run {
                    hideKeyboard(this)
                }
            }
            transaction.add(containerId, fragment)
            transaction.show(fragment)
            fragment.view?.run {
                hideKeyboard(this)
            }
            onFragmentChanged(fragment)
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
                onFragmentChanged(last)
            }.commit()
            true
        }
    }

    private fun onFragmentChanged(fragment: Fragment) {
        val page = when (fragment) {
            is LoadingPage -> Page.LOADING
            is MessagesPage -> Page.MESSAGES
            is OfflineFormPage -> Page.OFFLINE_FORM
            is OfflineFormSelectorPage -> Page.OFFLINE_FORM_SELECTOR
            else -> return
        }
        pageSubject.onNext(page)
    }

    fun pageRx(): Observable<Page> = pageSubject

    fun setSubjectIndex(index: Int) {
        fragmentManager.fragments.forEach {
            if (it is OfflineFormPage) {
                it.setSubjectIndex(index)
            }
        }
    }

    enum class Page {
        LOADING,
        MESSAGES,
        OFFLINE_FORM,
        OFFLINE_FORM_SELECTOR
    }
}