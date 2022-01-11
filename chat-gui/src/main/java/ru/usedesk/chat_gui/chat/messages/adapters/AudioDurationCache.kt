package ru.usedesk.chat_gui.chat.messages.adapters

import android.media.MediaPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AudioDurationCache {
    private val durations = hashMapOf<String, Int>()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val jobs = hashMapOf<String, Job>()
    private val mutex = Mutex()

    fun loadDuration(url: String, onLoaded: (Int) -> Unit) {
        val cachedDuration = durations[url]
        if (cachedDuration != null) {
            onLoaded(cachedDuration)
        } else {
            runBlocking {
                mutex.withLock {
                    jobs[url] = jobs[url] ?: ioScope.launch {
                        val duration = loadAudioDuration(url)
                        mutex.withLock {
                            if (duration != null) {
                                durations[url] = duration
                            }
                            jobs.remove(url)
                        }
                        yield()

                        onLoaded(duration ?: 0)
                    }
                }
            }
        }
    }

    fun cancel(url: String) {
        runBlocking {
            mutex.withLock {
                jobs[url]?.let { job ->
                    jobs.remove(url)
                    job.cancel()
                }
            }
        }
    }

    fun cancelAll() {
        runBlocking {
            mutex.withLock {
                jobs.values.forEach { job ->
                    job.cancel()
                }
                jobs.clear()
            }
        }
    }

    private suspend fun loadAudioDuration(url: String): Int? {
        return try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
            }
            yield()
            val duration = mediaPlayer.duration / 1000
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            null
        }
    }
}