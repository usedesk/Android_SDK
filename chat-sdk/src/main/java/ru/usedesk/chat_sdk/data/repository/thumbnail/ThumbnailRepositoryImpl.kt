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

internal class ThumbnailRepositoryImpl @Inject constructor(
    private val appContext: Context
) : ThumbnailRepository {
    private val cacheDir = appContext.cacheDir
    private val handledSet = mutableSetOf<String>()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    override val thumbnailMapFlow = MutableStateFlow<Map<String, Uri>>(mapOf())

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

    private fun String.toFile(): File =
        File(cacheDir, "thumbnail_${replace('-', '_')}.jpg")

    private fun launchLoadThumbnail(
        id: String,
        localId: String,
        videoUri: Uri
    ) {
        ioScope.launch {
            var thumbnailUri: Uri? = null
            var attempt = 0
            while (thumbnailUri == null && attempt < ATTEMPTS) {
                if (attempt > 0) {
                    delay(RETRY_DELAY * attempt)
                }
                attempt++
                thumbnailUri = loadThumbnailOnce(id, localId, videoUri)
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

    private fun loadThumbnailOnce(
        id: String,
        localId: String,
        videoUri: Uri
    ): Uri? {
        val thumbnailFile = localId.toFile()
        if (thumbnailFile.exists()) {
            return when (localId) {
                id -> thumbnailFile.toUri()
                else -> {
                    val newThumbnailFile = id.toFile()
                    thumbnailFile.renameTo(newThumbnailFile)
                    newThumbnailFile.toUri()
                }
            }
        }
        val thumbnailBitmap = extractFrame(videoUri) ?: return null
        return try {
            thumbnailFile.outputStream().use { out ->
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            thumbnailFile.toUri()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            thumbnailBitmap.recycle()
        }
    }

    private fun extractFrame(videoUri: Uri): Bitmap? {
        val path = videoUri.toString()
        val media = MediaMetadataRetriever()
        return try {
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
            media.getFrameAtTime(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            media.release()
        }
    }

    companion object {
        private const val ATTEMPTS = 3
        private const val RETRY_DELAY = 5000L
    }
}
