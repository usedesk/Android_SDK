package ru.usedesk.sample

import androidx.multidex.MultiDexApplication
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_sdk.UsedeskLog

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        UsedeskLog.enable()
        ServiceLocator.init(this)

        val isThemeMaterialComponentsUsed = false
        if (isThemeMaterialComponentsUsed) {
            mapOf(
                R.style.Usedesk_Chat_Screen_Messages_Page to R.style.Custom_Chat_Screen_Messages_Page,
                R.style.Usedesk_Chat_Screen_Offline_Form_Page to R.style.Custom_Chat_Screen_Offline_Form_Page,
                R.style.Usedesk_KnowledgeBase_Article_Content_Page_Item to R.style.Custom_KnowledgeBase_Article_Content_Page_Item
            ).forEach {
                UsedeskResourceManager.replaceResourceId(it.key, it.value)
            }
        }
    }
}