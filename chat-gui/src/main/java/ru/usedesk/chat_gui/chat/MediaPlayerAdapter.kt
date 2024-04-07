package ru.usedesk.chat_gui.chat

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.doOnLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.IUsedeskOnFullscreenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.hideKeyboard
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.common_gui.visibleGone
import ru.usedesk.common_gui.visibleInvisible
import ru.usedesk.common_sdk.api.IUsedeskOkHttpClientFactory

@UnstableApi
internal class MediaPlayerAdapter(
    fragment: UsedeskFragment,
    private val playerViewModel: PlayerViewModel,
    private val usedeskOkHttpClientFactory: IUsedeskOkHttpClientFactory
) {
    private var fullscreenListener = fragment.findParent<IUsedeskOnFullscreenListener>()
    private var downloadListener = fragment.findParent<IUsedeskOnDownloadListener>()

    private val pvVideoExoPlayer = inflateItem(
        fragment.view as ViewGroup,
        R.layout.usedesk_view_player,
        R.style.Usedesk_Chat_Player_Video
    ) { rootView, _ -> rootView as PlayerView }

    private val pvAudioExoPlayer = inflateItem(
        fragment.view as ViewGroup,
        R.layout.usedesk_view_player,
        R.style.Usedesk_Chat_Player_Audio
    ) { rootView, _ -> rootView as PlayerView }

    private var restored = playerViewModel.modelFlow.value.key.isNotEmpty()

    private val exoPlayer: ExoPlayer = playerViewModel.exoPlayer
        ?: ExoPlayer.Builder(fragment.requireContext())
            .apply {
                val okHttpClient = usedeskOkHttpClientFactory.createInstance()
                val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                setMediaSourceFactory(DefaultMediaSourceFactory(okHttpDataSourceFactory))
            }
            .build().also {
                playerViewModel.exoPlayer = it
                restored = false
            }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            when {
                player.playbackState == Player.STATE_BUFFERING -> setControlsVisibility(loader = true)
                player.isPlaying -> setControlsVisibility(pause = true)
                else -> setControlsVisibility(play = true)
            }
        }
    }

    private var lastMinimizeView: MinimizeView? = null
    private var currentMinimizeView: MinimizeView? = null

    private val videoBinding = VideoExoPlayerBinding(pvVideoExoPlayer)
    private val audioBinding = AudioExoPlayerBinding(pvAudioExoPlayer)

    private fun playAfterPause() {
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.play()
    }

    init {
        audioBinding.apply {
            contentFrame.visibility = View.GONE
            buttonPlay.setOnClickListener { playAfterPause() }
            buttonPause.setOnClickListener { exoPlayer.pause() }
        }

        videoBinding.apply {
            ivDownload.setOnClickListener {
                val model = playerViewModel.modelFlow.value
                downloadListener?.onDownload(model.key, model.name)
            }
            ivDownload.visibility = visibleGone(downloadListener != null)

            fullscreenButton.setOnClickListener {
                playerViewModel.fullscreen()
            }
            fullscreenButton.visibility = visibleGone(fullscreenListener != null)
            buttonPlay.setOnClickListener { playAfterPause() }
            buttonPause.setOnClickListener { exoPlayer.pause() }
        }

        exoPlayer.addListener(playerListener)

        pvVideoExoPlayer.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            onControllerBarHeightChanged(visibility == View.VISIBLE)
        })

        playerViewModel.modelFlow.onEachWithOld(fragment.lifecycleScope) { old, new ->
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
                        else -> {}
                    }
                } //Fullscreen will handled only if videoKey is not changed
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

                pvVideoExoPlayer.setControllerVisibilityListener(null as? PlayerView.ControllerVisibilityListener?)
                exoPlayer.removeListener(playerListener)
                fullscreenListener = null
                downloadListener = null
                lastMinimizeView = null
                currentMinimizeView = null
            }

            override fun onResume(owner: LifecycleOwner) {
                val model = playerViewModel.modelFlow.value
                if (model.fullscreen && model.mode == PlayerViewModel.Mode.VIDEO_EXO_PLAYER) {
                    fullscreenListener?.onFullscreenChanged(true)
                }
            }
        })
    }

    private fun setControlsVisibility(
        loader: Boolean = false,
        play: Boolean = false,
        pause: Boolean = false
    ) {
        videoBinding.apply {
            pbLoading.visibility = visibleInvisible(loader)
            buttonPlay.visibility = visibleGone(play)
            buttonPause.visibility = visibleGone(pause)
        }
        audioBinding.apply {
            buttonPlay.visibility = visibleGone(play || loader)
            buttonPause.visibility = visibleGone(pause)
        }
    }

    private fun onControllerBarHeightChanged(visible: Boolean) {
        videoBinding.lBottomBar.doOnLayout {
            currentMinimizeView?.onControlsHeightChanged?.invoke(
                when {
                    visible -> it.measuredHeight
                    else -> 0
                }
            )
        }
    }

    private fun changeFullscreen(fullscreen: Boolean) {
        when (playerViewModel.modelFlow.value.mode) {
            PlayerViewModel.Mode.VIDEO_EXO_PLAYER -> {
                (pvVideoExoPlayer.parent as? ViewGroup)?.removeView(pvVideoExoPlayer)

                if (fullscreen) {
                    fullscreenListener?.getFullscreenLayout()?.addView(pvVideoExoPlayer)
                    videoBinding.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_exit)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvVideoExoPlayer)
                    videoBinding.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_enter)
                }
                //Each time need to set player again, otherwise it will not know what happened
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
                //Each time need to set player again, otherwise it will not know what happened
                pvAudioExoPlayer.player = exoPlayer
                fullscreenListener?.getFullscreenLayout()?.visibility = visibleGone(fullscreen)
                fullscreenListener?.onFullscreenChanged(fullscreen)
                pvAudioExoPlayer.showController()
            }
            else -> {}
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

        //Current player will be old
        lastMinimizeView = currentMinimizeView

        //Reset old player, if it is
        resetPlayer()

        //Saving data for new playing
        currentMinimizeView = MinimizeView(lMinimized, onCancel, onControlsHeightChanged)

        when (playerType) {
            PlayerType.VIDEO -> playerViewModel.videoApply(mediaKey, mediaName)
            PlayerType.AUDIO -> playerViewModel.audioApply(mediaKey, mediaName)
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
        val model = playerViewModel.modelFlow.value
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
        val model = playerViewModel.modelFlow.value
        return when (model.key) {
            mediaKey -> {
                //Saving data for player
                currentMinimizeView = MinimizeView(
                    lMinimized,
                    onCancel,
                    onControlsHeightChanged
                )

                changeFullscreen(model.fullscreen)

                onControllerBarHeightChanged(pvVideoExoPlayer.isControllerFullyVisible)
                playIfLastPlaying()
                true
            }
            else -> false
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
        val lBottomBar = exoPlayerView.findViewById<View>(R.id.exo_bottom_bar)
        val pbLoading = exoPlayerView.findViewById<ProgressBar>(R.id.loading)
        val ivDownload = exoPlayerView.findViewById<View>(R.id.iv_download)
        val buttonPlay = exoPlayerView.findViewById<View>(R.id.exo_play)
        val buttonPause = exoPlayerView.findViewById<View>(R.id.exo_pause)
    }

    private class AudioExoPlayerBinding(exoPlayerView: PlayerView) {
        val contentFrame = exoPlayerView.findViewById<View>(R.id.exo_content_frame)
        val buttonPlay = exoPlayerView.findViewById<View>(R.id.exo_play)
        val buttonPause = exoPlayerView.findViewById<View>(R.id.exo_pause)
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