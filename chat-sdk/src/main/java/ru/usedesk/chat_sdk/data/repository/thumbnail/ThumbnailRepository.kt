
package ru.usedesk.chat_sdk.data.repository.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import java.io.File
import javax.inject.Inject

internal class ThumbnailRepository @Inject constructor(
    private val appContext: Context
) : IThumbnailRepository {
    private val cacheDir = appContext.cacheDir
    private val handledSet = mutableSetOf<Long>()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    override val thumbnailMapFlow = MutableStateFlow<Map<Long, Uri>>(mapOf())

    override fun loadThumbnail(message: UsedeskMessage.File) {
        if (message.file.isVideo()) {
            ioScope.launch {
                val id = message.id
                val localId = (message as? UsedeskMessageOwner.Client)?.localId ?: id
                mutex.withLock {
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
                when (localId) {
                    id -> thumbnailFile.toUri()
                    else -> {
                        val newThumbnailFile = id.toFile()
                        thumbnailFile.renameTo(newThumbnailFile)
                        newThumbnailFile.toUri()
                    }
                }
            } else {
                val path = videoUri.toString()
                val media = MediaMetadataRetriever()
                val thumbnailBitmap = try {
                    try {
                        media.setDataSource(path)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    } ?: try {
                        media.setDataSource(path, mapOf())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    } ?: try {
                        media.setDataSource(appContext, videoUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //Different methods works on different devices

                    media.getFrameAtTime(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    media.release()
                }
                try {
                    when (thumbnailBitmap) {
                        null -> null
                        else -> thumbnailFile.outputStream().use { out ->
                            thumbnailBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                100,
                                out
                            )
                            thumbnailFile.toUri()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    thumbnailBitmap?.recycle()
                }
            }
            if (thumbnailUri == null) {
                delay(5000)
            }
            mutex.withLock {
                when (thumbnailUri) {
                    null -> handledSet.remove(id)
                    else -> thumbnailMapFlow.value = thumbnailMapFlow.value.toMutableMap().apply {
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