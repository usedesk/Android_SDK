package ru.usedesk.chat_sdk.entity

import android.net.Uri

data class UsedeskFileInfo(val uri: Uri, val type: Type) {

    enum class Type {
        IMAGE,
        VIDEO,
        DOCUMENT,
        OTHER;

        companion object {
            fun getByMimeType(mimeType: String?): Type {
                if (mimeType != null) {
                    when {
                        mimeType.startsWith("image/") -> {
                            return IMAGE
                        }
                        mimeType.startsWith("video/") -> {
                            return VIDEO
                        }
                        mimeType.startsWith("doc/") -> {
                            return DOCUMENT
                        }
                    }
                }
                return OTHER
            }
        }
    }
}