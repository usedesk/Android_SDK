package ru.usedesk.common_sdk

import android.util.Log

object UsedeskLog {
    private var logsEnabled = BuildConfig.DEBUG

    fun enable() {
        logsEnabled = true
    }

    fun disable() {
        logsEnabled = false
    }

    fun d(logPrefix: String, logText: () -> String) {
        if (logsEnabled) {
            Log.d("USEDESK", getFullLog(logPrefix, logText))
        }
    }

    fun e(logPrefix: String, logText: () -> String) {
        if (logsEnabled) {
            Log.e("USEDESK", getFullLog(logPrefix, logText))
        }
    }

    private fun getFullLog(logPrefix: String, logText: () -> String) = "$logPrefix: ${logText()}"
}