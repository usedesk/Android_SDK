package ru.usedesk.sample.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.chat.UsedeskChatScreen.Companion.newInstance
import ru.usedesk.chat_gui.showfile.UsedeskShowFileScreen.Companion.newInstance
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen.Companion.newInstance
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen

class MainNavigation internal constructor(private val activity: AppCompatActivity, private val containerId: Int) {
    private fun switchFragment(fragment: Fragment) {
        activity.supportFragmentManager
                .beginTransaction()
                .addToBackStack(fragment.javaClass.name + ":" + fragment.hashCode())
                .replace(containerId, fragment)
                .commit()
    }

    fun goConfiguration() {
        switchFragment(ConfigurationScreen.newInstance())
    }

    fun goChat(
            customAgentName: String?,
            rejectedFileExtensions: Collection<String>
    ) {
        switchFragment(newInstance(customAgentName, rejectedFileExtensions))
    }

    fun goKnowledgeBase(withSupportButton: Boolean,
                        withArticleRating: Boolean) {
        switchFragment(newInstance(withSupportButton, withArticleRating))
    }

    fun goShowFile(usedeskFile: UsedeskFile) {
        switchFragment(newInstance(usedeskFile))
    }

    fun onBackPressed() {
        val fragmentManager = activity.supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            val fragment = fragmentManager.fragments[0]
            if (fragment is UsedeskFragment && fragment.onBackPressed()) {
                return
            }
            if (fragment is UsedeskChatScreen) {
                fragment.clear()
            }
            fragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }
}