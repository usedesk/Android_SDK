package ru.usedesk.chat_gui.chat.data.thumbnail

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
import java.io.File
import javax.inject.Inject

internal class ThumbnailRepository @Inject constructor(
    private val appContext: Context
) : IThumbnailRepository {
    override val thumbnailMapFlow = MutableStateFlow<Map<Long, Uri>>(mapOf())
    private val handledSet = mutableSetOf<Long>()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val cacheDir = appContext.cacheDir

    init {
        println()
    }

    override fun loadThumbnail(message: UsedeskMessage.File) {
        if (message.file.isVideo()) {
            ioScope.launch {
                mutex.withLock {
                    val id = message.id
                    val localId = (message as? UsedeskMessageOwner.Client)?.localId ?: id
                    if (!handledSet.contains(localId) || !handledSet.contains(id)) {
                        handledSet.add(id)
                        launchLoadPreview(id, localId, message.file.content.toUri())
                    }
                }
            }
        }
    }

    private fun Long.toFile() = File(cacheDir, "thumbnail_${toString().replace('-', '_')}.jpg")

    private fun launchLoadPreview(id: Long, localId: Long, videoUri: Uri) {
        ioScope.launch {
            val thumbnailFile = localId.toFile()
            var thumbnailUri: Uri? = null
            if (thumbnailFile.exists()) {
                thumbnailUri = when (localId) {
                    id -> thumbnailFile.toUri()
                    else -> {
                        val newThumbnailFile = id.toFile()
                        thumbnailFile.renameTo(newThumbnailFile)
                        newThumbnailFile.toUri()
                    }
                }
            } else {
                val media = MediaMetadataRetriever()
                while (true) {
                    try {
                        media.setDataSource(videoUri.toString())
                        val thumbnailBitmap = media.getFrameAtTime(0)
                        if (thumbnailBitmap != null) {
                            thumbnailFile.outputStream().use { out ->
                                thumbnailBitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    out
                                )
                            }
                            thumbnailUri = thumbnailFile.toUri()
                        }
                        break
                    } catch (e: Exception) {
                        e.printStackTrace()
                        delay(5000)
                    }
                }
                media.release()
            }
            if (thumbnailUri != null) {
                mutex.withLock {
                    val map = thumbnailMapFlow.value.toMutableMap().apply {
                        set(localId, thumbnailUri)
                    }
                    thumbnailMapFlow.value = map
                }
            }
        }
    }
}