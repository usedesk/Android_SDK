package ru.usedesk.chat_gui.chat

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.IUsedeskOnFullscreenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.*

internal class MediaPlayerAdapter(
    fragment: UsedeskFragment,
    private val playerViewModel: PlayerViewModel
) {
    private var fullscreenListener = fragment.findParent<IUsedeskOnFullscreenListener>()
    private var downloadListener = fragment.findParent<IUsedeskOnDownloadListener>()

    private val pvVideoExoPlayer = inflateItem(
        fragment.view as ViewGroup,
        R.layout.usedesk_view_player,
        R.style.Usedesk_Chat_Player_Video
    ) { rootView, defaultStyleId ->
        rootView as PlayerView
    }

    private val pvAudioExoPlayer = inflateItem(
        fragment.view as ViewGroup,
        R.layout.usedesk_view_player,
        R.style.Usedesk_Chat_Player_Audio
    ) { rootView, defaultStyleId ->
        rootView as PlayerView
    }

    private var restored = playerViewModel.modelLiveData.value.key.isNotEmpty()

    private val exoPlayer: ExoPlayer = playerViewModel.exoPlayer
        ?: ExoPlayer.Builder(fragment.requireContext())
            .build().also {
                playerViewModel.exoPlayer = it
                restored = false
            }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            videoBinding.pbLoading.visibility =
                visibleInvisible(playbackState == Player.STATE_BUFFERING)
        }
    }

    private var lastMinimizeView: MinimizeView? = null
    private var currentMinimizeView: MinimizeView? = null

    private val videoBinding = VideoExoPlayerBinding(pvVideoExoPlayer)
    private val audioBinding = AudioExoPlayerBinding(pvAudioExoPlayer)

    init {
        audioBinding.contentFrame.updateLayoutParams {
            width = 0
            height = 0
        }

        exoPlayer.addListener(playerListener)

        pvVideoExoPlayer.setControllerVisibilityListener { visibility ->
            val visible = visibility == View.VISIBLE
            videoBinding.controls.startAnimation(
                AnimationUtils.loadAnimation(
                    fragment.requireContext(),
                    if (visible) {
                        R.anim.usedesk_fade_in
                    } else {
                        R.anim.usedesk_fade_out
                    }
                )
            )
            postControllerBarHeight(visible)
        }

        videoBinding.ivDownload.setOnClickListener {
            val model = playerViewModel.modelLiveData.value
            downloadListener?.onDownload(model.key, model.name)
        }
        videoBinding.ivDownload.visibility = visibleGone(downloadListener != null)

        audioBinding.tvDownload.setOnClickListener {
            val model = playerViewModel.modelLiveData.value
            downloadListener?.onDownload(model.key, model.name)
        }
        audioBinding.tvDownload.visibility = visibleGone(downloadListener != null)

        videoBinding.fullscreenButton.setOnClickListener {
            playerViewModel.fullscreen()
        }
        videoBinding.fullscreenButton.visibility = visibleGone(fullscreenListener != null)

        playerViewModel.modelLiveData.initAndObserveWithOld(fragment) { old, new ->
            if (!restored && old?.mode != new.mode) {
                resetPlayer()
            }
            if (new.key.isNotEmpty()) {
                if (!restored && old?.key != new.key) {
                    when (new.mode) {
                        PlayerViewModel.Mode.VIDEO_EXO_PLAYER,
                        PlayerViewModel.Mode.AUDIO_EXO_PLAYER -> {
                            exoPlayer.run {
                                setMediaItem(MediaItem.fromUri(new.key))
                                prepare()
                                play()
                            }
                            if (new.mode == PlayerViewModel.Mode.VIDEO_EXO_PLAYER) {
                                pvVideoExoPlayer.hideController()
                            } else {
                                pvAudioExoPlayer.showController()
                            }
                            changeFullscreen(false)
                        }
                    }
                }//Фулскрин обрабатываем только если не был обновлён videoKey
                else if (restored || old?.fullscreen != new.fullscreen) {
                    changeFullscreen(new.fullscreen)
                    if (restored) {
                        restored = false
                        playIfLastPlaying()
                    }
                }
            }
        }

        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                playerViewModel.lastPlaying = exoPlayer.isPlaying || exoPlayer.isLoading
                exoPlayer.pause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                resetPlayer()

                pvVideoExoPlayer.setControllerVisibilityListener(null)
                exoPlayer.removeListener(playerListener)
                fullscreenListener = null
                downloadListener = null
                lastMinimizeView = null
                currentMinimizeView = null
            }

            override fun onResume(owner: LifecycleOwner) {
                val model = playerViewModel.modelLiveData.value
                if (model.fullscreen &&
                    (model.mode == PlayerViewModel.Mode.VIDEO_EXO_PLAYER)
                ) {
                    fullscreenListener?.onFullscreenChanged(true)
                }
            }
        })
    }

    private fun postControllerBarHeight(visible: Boolean) {
        pvVideoExoPlayer.post {
            currentMinimizeView?.onControlsHeightChanged?.invoke(
                if (visible) {
                    videoBinding.lBottomBar?.height ?: 0
                } else {
                    0
                }
            )
        }
    }

    private fun changeFullscreen(fullscreen: Boolean) {
        when (playerViewModel.modelLiveData.value.mode) {
            PlayerViewModel.Mode.VIDEO_EXO_PLAYER -> {
                (pvVideoExoPlayer.parent as? ViewGroup)?.removeView(pvVideoExoPlayer)

                if (fullscreen) {
                    fullscreenListener?.getFullscreenLayout()?.addView(pvVideoExoPlayer)
                    videoBinding.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_exit)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvVideoExoPlayer)
                    videoBinding.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_enter)
                }
                //Каждый раз задаём плеер, иначе он не поймёт что только что произошло
                pvVideoExoPlayer.player = exoPlayer
                fullscreenListener?.getFullscreenLayout()?.visibility = visibleGone(fullscreen)
                fullscreenListener?.onFullscreenChanged(fullscreen)
            }
            PlayerViewModel.Mode.AUDIO_EXO_PLAYER -> {
                (pvAudioExoPlayer.parent as? ViewGroup)?.removeView(pvAudioExoPlayer)

                if (fullscreen) {
                    fullscreenListener?.getFullscreenLayout()?.addView(pvAudioExoPlayer)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvAudioExoPlayer)
                }
                //Каждый раз задаём плеер, иначе он не поймёт что только что произошло
                pvAudioExoPlayer.player = exoPlayer
                fullscreenListener?.getFullscreenLayout()?.visibility = visibleGone(fullscreen)
                fullscreenListener?.onFullscreenChanged(fullscreen)
                pvAudioExoPlayer.showController()
            }
        }
    }

    /**
     * @return true if event was handled
     */
    fun onBackPressed(): Boolean = playerViewModel.onBackPressed()

    /**
     * Resets the current playback and switches the player's view to lMinimized.
     *
     * @param lMinimized Parent view for minimized player
     * @param mediaKey URL of the video/audio source
     * @param mediaName Video/audio name
     * @param playerType Video/audio type
     * @param onCancel Called when the player cancels playing the current media
     * @param onControlsHeightChanged Called when player controls are visible
     */
    fun attachPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        mediaName: String,
        playerType: PlayerType,
        onCancel: () -> Unit,
        onControlsHeightChanged: ((Int) -> Unit) = {}
    ) {
        hideKeyboard(pvVideoExoPlayer)

        //Текущий плеер станет старым
        lastMinimizeView = currentMinimizeView

        //Сбросим старое воспроизведение, если оно было
        resetPlayer()

        //Сохраним данные для нового воспроизведения
        currentMinimizeView = MinimizeView(lMinimized, onCancel, onControlsHeightChanged)

        when (playerType) {
            PlayerType.VIDEO -> {
                playerViewModel.videoApply(mediaKey, mediaName)
            }
            PlayerType.AUDIO -> {
                playerViewModel.audioApply(mediaKey, mediaName)
            }
        }
    }

    private fun resetPlayer() {
        exoPlayer.stop()
        pvVideoExoPlayer.player = null
        pvAudioExoPlayer.player = null
        (pvVideoExoPlayer.parent as? ViewGroup)?.removeView(pvVideoExoPlayer)
        (pvAudioExoPlayer.parent as? ViewGroup)?.removeView(pvAudioExoPlayer)
        lastMinimizeView?.release()
        lastMinimizeView = null
        fullscreenListener?.getFullscreenLayout()?.visibility = View.GONE
    }

    /**
     * Detach current player view if mediaKey is equal to the mediaKey of current player.
     */
    fun detachPlayer(key: String): Boolean {
        val model = playerViewModel.modelLiveData.value
        return if (model.key == key && !model.fullscreen) {
            lastMinimizeView = currentMinimizeView
            resetPlayer()

            playerViewModel.reset()
            true
        } else {
            false
        }
    }

    /**
     * Places player view inside lMinimized if mediaKey is equal to the mediaKey of current player.
     *
     * @param lMinimized Parent view for minimized player
     * @param mediaKey URL of the video/audio source
     * @param onCancel Called when the player cancels playing the current media
     * @param onControlsHeightChanged Called when player controls are visible
     *
     * @return true if player was reattached
     */
    fun reattachPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        onCancel: () -> Unit,
        onControlsHeightChanged: ((Int) -> Unit) = {}
    ): Boolean {
        val model = playerViewModel.modelLiveData.value
        return if (model.key == mediaKey) {
            //Сохраним данные для плеера
            currentMinimizeView = MinimizeView(
                lMinimized,
                onCancel,
                onControlsHeightChanged
            )

            changeFullscreen(model.fullscreen)

            postControllerBarHeight(pvVideoExoPlayer.isControllerVisible)
            playIfLastPlaying()
            true
        } else {
            false
        }
    }

    private fun playIfLastPlaying() {
        if (playerViewModel.lastPlaying) {
            playerViewModel.lastPlaying = false

            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    private class VideoExoPlayerBinding(exoPlayerView: PlayerView) {
        val fullscreenButton = exoPlayerView.findViewById<ImageView>(R.id.exo_fullscreen_icon)
        val controls = exoPlayerView.findViewById<View>(R.id.exo_controller)
        val lBottomBar = exoPlayerView.findViewById<View>(R.id.l_bottom_bar)
        val pbLoading = exoPlayerView.findViewById<ProgressBar>(R.id.loading)
        val ivDownload = exoPlayerView.findViewById<View>(R.id.iv_download)
    }

    private class AudioExoPlayerBinding(exoPlayerView: PlayerView) {
        val contentFrame = exoPlayerView.findViewById<View>(R.id.exo_content_frame)
        val tvDownload = exoPlayerView.findViewById<View>(R.id.tv_download)
    }

    private class MinimizeView(
        val lVideoMinimized: ViewGroup,
        val onCancelPlay: () -> Unit,
        val onControlsHeightChanged: (Int) -> Unit
    ) {
        fun release() {
            onControlsHeightChanged.invoke(0)
            onCancelPlay.invoke()
        }
    }

    enum class PlayerType {
        VIDEO,
        AUDIO
    }
}