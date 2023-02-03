package ru.usedesk.chat_sdk.data.repository.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import ru.usedesk.common_sdk.UsedeskLog
import java.io.File
import javax.inject.Inject

internal class ThumbnailRepository @Inject constructor(
    appContext: Context
) : IThumbnailRepository {
    private val cacheDir = appContext.cacheDir

    private val handledSet = mutableSetOf<Long>()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    override val thumbnailMapFlow = MutableStateFlow<Map<Long, Uri>>(mapOf())

    override fun loadThumbnail(message: UsedeskMessage.File) {
        if (message.file.isVideo()) {
            ioScope.launch {
                mutex.withLock {
                    val id = message.id
                    val localId = (message as? UsedeskMessageOwner.Client)?.localId ?: id
                    if (!handledSet.contains(localId) || !handledSet.contains(id)) {
                        handledSet.add(id)
                        launchLoadThumbnail(id, localId, message.file.content.toUri())
                    }
                }
            }
        }
    }

    private fun Long.toFile(): File =
        File(cacheDir, "thumbnail_${toString().replace('-', '_')}.jpg")

    private fun launchLoadThumbnail(id: Long, localId: Long, videoUri: Uri) {
        ioScope.launch {
            val thumbnailFile = localId.toFile()
            val thumbnailUri = if (thumbnailFile.exists()) {
                UsedeskLog.onLog("exist") { thumbnailFile.toString() }
                when (localId) {
                    id -> thumbnailFile.toUri()
                    else -> {
                        val newThumbnailFile = id.toFile()
                        UsedeskLog.onLog("renamed") { "$thumbnailFile to $newThumbnailFile" }
                        thumbnailFile.renameTo(newThumbnailFile)
                        newThumbnailFile.toUri()
                    }
                }
            } else {
                val media = MediaMetadataRetriever()
                try {
                    val path = videoUri.toString()
                    when (videoUri.scheme) {
                        "file" -> media.setDataSource(path)
                        else -> media.setDataSource(path, mapOf())
                    }
                    when (val thumbnailBitmap = media.getFrameAtTime(0)) {
                        null -> null
                        else -> {
                            thumbnailFile.outputStream().use { out ->
                                thumbnailBitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    out
                                )
                            }
                            thumbnailFile.toUri()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    media.release()
                }
            }
            if (thumbnailUri == null) {
                delay(5000)
            }
            mutex.withLock {
                when (thumbnailUri) {
                    null -> handledSet.remove(id)
                    else -> thumbnailMapFlow.value = thumbnailMapFlow.value.toMutableMap().apply {
                        UsedeskLog.onLog(localId.toString()) { thumbnailUri.toString() }
                        UsedeskLog.onLog(id.toString()) { thumbnailUri.toString() }
                        set(localId, thumbnailUri)
                        if (localId != id) {
                            set(id, thumbnailUri)
                        }
                    }
                }
            }
        }
    }
}