
package ru.usedesk.common_gui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class UsedeskSnackbar private constructor() {
    companion object {

        fun create(
            parentView: View,
            backgroundColor: Int,
            messageText: String,
            messageColor: Int,
            actionText: String,
            actionColor: Int
        ): Snackbar {
            return create(parentView, backgroundColor, messageText, messageColor).apply {
                view.findViewById<TextView>(R.id.snackbar_text).apply {
                    gravity = Gravity.START
                }
                setAction(actionText) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts(
                            "package",
                            view.context.packageName,
                            null
                        )
                    }
                    view.context.startActivity(intent)
                }
                setActionTextColor(actionColor)
            }
        }

        fun create(
            parentView: View,
            backgroundColor: Int,
            messageText: String,
            messageColor: Int
        ): Snackbar {
            return Snackbar.make(parentView, messageText, Snackbar.LENGTH_LONG).apply {
                view.setBackgroundColor(backgroundColor)
                setTextColor(messageColor)
                view.findViewById<TextView>(R.id.snackbar_text).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        }
    }
}