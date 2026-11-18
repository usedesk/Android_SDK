package ru.usedesk.sample

import android.app.Application
import ru.usedesk.common_sdk.UsedeskLog

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        UsedeskLog.enable()
        ServiceLocator.instance = ServiceLocator(this)
    }
}