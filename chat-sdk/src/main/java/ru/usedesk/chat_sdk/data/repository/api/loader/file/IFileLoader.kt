
package ru.usedesk.chat_sdk.data.repository.api.loader.file

import android.net.Uri

internal interface IFileLoader {
    suspend fun save(uri: Uri): Uri
    suspend fun remove(cachedUri: Uri)
}