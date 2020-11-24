package ru.usedesk.chat_sdk.internal.domain.entity

class UsedeskFile(val content: String, val type: String, val size: String, val name: String) {
    val isImage: Boolean
        get() = type.startsWith(IMAGE_TYPE) || endsLikeImage()

    private fun endsLikeImage(): Boolean {
        return name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".bmp") || name.endsWith(".jpeg")
    }

    companion object {
        private const val IMAGE_TYPE = "image/"
    }
}