package ru.usedesk.chat_sdk.service.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskForegroundNotificationsService
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskNotificationsService
import ru.usedesk.common_sdk.UsedeskLog

@Suppress("DEPRECATION")
internal object NotificationsServiceController {
    private val handler = Handler(Looper.getMainLooper())
    private val entries = mutableMapOf<Class<*>, Entry>()

    private class Entry(val context: Context, val serviceClass: Class<*>) {
        var requested = false
        var configuration: UsedeskChatConfiguration? = null
        var pending = false
        var service: UsedeskNotificationsService? = null
        var stopping = false
        var restartAfterStop = false
    }

    fun start(context: Context, serviceClass: Class<*>, configuration: UsedeskChatConfiguration) = onMain {
        val entry = entries.getOrPut(serviceClass) { Entry(context.applicationContext, serviceClass) }
        entry.requested = true
        entry.configuration = configuration
        when {
            entry.stopping -> entry.restartAfterStop = true
            entry.service != null && !entry.pending -> entry.service?.startListening(configuration)
            !entry.pending -> launch(entry)
        }
    }

    fun stop(serviceClass: Class<*>) = onMain {
        entries[serviceClass]?.let { entry ->
            entry.requested = true
            entry.configuration = null
            entry.restartAfterStop = false
            entry.service?.requestStop()
        }
    }

    fun stopAll() = onMain {
        entries.values.toList().forEach { entry ->
            entry.requested = true
            entry.configuration = null
            entry.restartAfterStop = false
            entry.service?.requestStop()
        }
    }

    fun created(service: UsedeskNotificationsService) {
        val entry = entries.getOrPut(service.javaClass) { Entry(service.applicationContext, service.javaClass) }
        entry.service = service
    }

    fun configuration(service: UsedeskNotificationsService, supplied: UsedeskChatConfiguration?): UsedeskChatConfiguration? {
        val entry = entries.getValue(service.javaClass)
        entry.pending = false
        if (!entry.requested) {
            entry.requested = true
            entry.configuration = supplied
        }
        return entry.configuration
    }

    fun stopping(service: UsedeskNotificationsService) {
        entries[service.javaClass]?.takeIf { it.service === service }?.stopping = true
    }

    fun failed(service: UsedeskNotificationsService) {
        entries[service.javaClass]?.takeIf { it.service === service }?.apply {
            requested = true
            configuration = null
            restartAfterStop = false
        }
        service.requestStop()
    }

    fun destroyed(service: UsedeskNotificationsService) {
        val entry = entries[service.javaClass] ?: return
        if (entry.service !== service) return
        entry.service = null
        entry.pending = false
        entry.stopping = false
        if (entry.restartAfterStop && entry.configuration != null) {
            entry.restartAfterStop = false
            launch(entry)
        } else {
            entries.remove(service.javaClass)
        }
    }

    private fun launch(entry: Entry) {
        val configuration = entry.configuration ?: return
        entry.pending = true
        try {
            val intent = Intent(entry.context, entry.serviceClass).putExtra(
                UsedeskNotificationsService.USEDESK_CHAT_CONFIGURATION_KEY,
                configuration
            )
            val component = if (Build.VERSION.SDK_INT >= 26 &&
                UsedeskForegroundNotificationsService::class.java.isAssignableFrom(entry.serviceClass)
            ) {
                entry.context.startForegroundService(intent)
            } else {
                entry.context.startService(intent)
            }
            if (component == null) {
                entries.remove(entry.serviceClass)
                UsedeskLog.onLog("NotificationsService") { "Service is not available: ${entry.serviceClass.name}" }
            }
        } catch (e: RuntimeException) {
            entries.remove(entry.serviceClass)
            UsedeskLog.onLog("NotificationsService") { "Start rejected: $e" }
        }
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else handler.post(block)
    }
}
