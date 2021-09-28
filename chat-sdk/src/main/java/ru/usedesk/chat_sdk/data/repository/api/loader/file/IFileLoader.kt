package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.net.Uri
import ru.usedesk.chat_sdk.data.repository.api.loader.file.entity.LoadedFile

internal interface IFileLoader {
    fun load(uri: Uri): LoadedFile
    fun copy(uriSource: Uri, uriDestination: Uri)
}