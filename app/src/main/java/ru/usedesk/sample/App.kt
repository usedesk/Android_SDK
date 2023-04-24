
package ru.usedesk.sample

import androidx.multidex.MultiDexApplication
import ru.usedesk.common_sdk.UsedeskLog

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        UsedeskLog.enable()
        ServiceLocator.instance = ServiceLocator(this)
    }
}