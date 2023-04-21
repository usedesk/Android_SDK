
package ru.usedesk.common_sdk

import android.util.Log

object UsedeskLog {
    private const val LOG_KEY = "USEDESK_DBG"

    private var logsEnabled = false
    private var listeners = mutableSetOf<(String) -> Unit>()

    fun enable() {
        logsEnabled = true
    }

    fun disable() {
        logsEnabled = false
    }

    fun addLogListener(logListener: (String) -> Unit) {
        listeners.add(logListener)
    }

    fun removeLogListener(logListener: (String) -> Unit) {
        listeners.remove(logListener)
    }

    fun onLog(logPrefix: String, log: () -> String) {
        if (logsEnabled) {
            val fullLog = "$logPrefix: ${log()}"
            Log.d(LOG_KEY, fullLog)
            listeners.forEach { listener ->
                listener(fullLog)
            }
        }
    }
}