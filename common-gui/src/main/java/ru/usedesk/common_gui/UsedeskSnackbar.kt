package ru.usedesk.common_gui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class UsedeskSnackbar private constructor() {
    companion object {

        fun create(
            fragment: Fragment,
            backgroundColor: Int,
            messageText: String,
            messageColor: Int,
            actionText: String,
            actionColor: Int
        ): Snackbar {
            return create(fragment, backgroundColor, messageText, messageColor).apply {
                view.findViewById<TextView>(R.id.snackbar_text).apply {
                    gravity = Gravity.START
                }
                setAction(actionText) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts(
                            "package",
                            fragment.requireContext().packageName,
                            null
                        )
                    }
                    fragment.startActivity(intent)
                }
                setActionTextColor(actionColor)
            }
        }

        fun create(
            fragment: Fragment,
            backgroundColor: Int,
            messageText: String,
            messageColor: Int
        ): Snackbar {
            return Snackbar.make(fragment.requireView(), messageText, Snackbar.LENGTH_LONG).apply {
                view.setBackgroundColor(backgroundColor)
                setTextColor(messageColor)
                view.findViewById<TextView>(R.id.snackbar_text).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }
        }
    }
}