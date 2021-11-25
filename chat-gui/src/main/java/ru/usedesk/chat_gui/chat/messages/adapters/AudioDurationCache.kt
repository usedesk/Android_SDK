package ru.usedesk.chat_gui.chat.messages.adapters

import android.media.MediaPlayer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AudioDurationCache {
    private val audioPreviews = hashMapOf<String, Int>()
    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }

    suspend fun getAudioDuration(url: String): Int {
        return suspendCoroutine { continuation ->
            val preview = audioPreviews[url] ?: loadAudioDuration(url)
            continuation.resume(preview)
        }
    }

    private fun loadAudioDuration(url: String): Int {
        return try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            mediaPlayer.duration / 1000
        } catch (e: Exception) {
            0
        }
    }

    fun release() {
        mediaPlayer.release()
    }
}