
package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.net.Uri

internal interface FileLoader {
    suspend fun save(uri: Uri): Uri?
    suspend fun remove(cachedUri: Uri)
}