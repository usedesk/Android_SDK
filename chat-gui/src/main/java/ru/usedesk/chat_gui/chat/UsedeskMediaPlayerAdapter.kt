package ru.usedesk.chat_gui.chat

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.hideKeyboard
import ru.usedesk.common_gui.visibleGone
import ru.usedesk.common_gui.visibleInvisible

class UsedeskMediaPlayerAdapter(
    activity: AppCompatActivity,
    private val lFullscreen: ViewGroup,
    private val onFullscreenMode: (Boolean) -> Unit
) : IUsedeskMediaPlayerAdapter {

    private val playerViewModel: PlayerViewModel by activity.viewModels()

    private val pvVideoExoPlayer = activity.layoutInflater.inflate(
        R.layout.usedesk_view_player_video,
        lFullscreen,
        false
    ) as PlayerView

    private val pvAudioExoPlayer = activity.layoutInflater.inflate(
        R.layout.usedesk_view_player_audio,
        lFullscreen,
        false
    ) as PlayerView

    /*private val pvYouTubePlayer =
        inflater.inflate(R.layout.view_player_youtube, lFullscreen, false) as YouTubePlayerView*/

    private val exoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(lFullscreen.context)
        .setUseLazyPreparation(true)
        .build()

    private var lastMinimizeView: MinimizeView? = null
    private var currentMinimizeView: MinimizeView? = null

    private val videoExoPlayerViews = VideoExoPlayerViews(pvVideoExoPlayer)
    private val audioExoPlayerViews = AudioExoPlayerViews(pvAudioExoPlayer)
    // private val youTubePlayerViews = YouTubePlayerViews(pvYouTubePlayer)

    init {
        //lifecycle.addObserver(pvYouTubePlayer)
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        onPause()
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        onResume()
                    }
                }
            }
        })

        audioExoPlayerViews.contentFrame.updateLayoutParams {
            width = 0
            height = 0
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                videoExoPlayerViews.pbLoading.visibility =
                    visibleInvisible(playbackState == Player.STATE_BUFFERING)
            }
        })

        pvVideoExoPlayer.setControllerVisibilityListener { visibility ->
            val visible = visibility == View.VISIBLE
            videoExoPlayerViews.controls.startAnimation(
                AnimationUtils.loadAnimation(
                    lFullscreen.context,
                    if (visible) {
                        R.anim.fade_in
                    } else {
                        R.anim.fade_out
                    }
                )
            )
            currentMinimizeView?.doOnControlsVisibilityChanged?.invoke(visible)
        }

        videoExoPlayerViews.fullscreenButton.setOnClickListener {
            playerViewModel.fullscreen()
        }

        /*youTubePlayerViews.fullscreenButton.setOnClickListener {
            playerViewModel.fullscreen()
        }

        youTubePlayerViews.progress?.updateLayoutParams {
            width = 0
            height = 0
        }

        youTubePlayerViews.youTubeButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=${playerViewModel.modelLiveData.value.key}")
            )
            try {
                it.context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, e.message ?: "Can't open url on YouTube")
            }
        }*/

        playerViewModel.modelLiveData.initAndObserveWithOld(activity) { old, new ->
            if (old?.mode != new.mode) {
                resetPlayer()
            }
            if (new.key.isNotEmpty()) {
                if (old?.key != new.key) {
                    when (new.mode) {
                        /*PlayerViewModel.Mode.YOU_TUBE_PLAYER -> {
                            pvYouTubePlayer.getYouTubePlayerWhenReady(object :
                                YouTubePlayerCallback {
                                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                    youTubePlayer.loadVideo(new.key, 0.0f)
                                }
                            })
                            changeFullscreen(false)
                        }*/
                        PlayerViewModel.Mode.VIDEO_EXO_PLAYER,
                        PlayerViewModel.Mode.AUDIO_EXO_PLAYER -> {
                            exoPlayer.run {
                                setMediaItem(MediaItem.fromUri(new.key))
                                exoPlayer.prepare()
                                exoPlayer.play()
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
                else if (old.fullscreen != new.fullscreen) {
                    changeFullscreen(new.fullscreen)
                }
            }
        }
    }

    private fun onPause() {
        when (playerViewModel.modelLiveData.value.mode) {
            /*PlayerViewModel.Mode.YOU_TUBE_PLAYER -> {
                pvYouTubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.pause()
                    }
                })
            }*/
            PlayerViewModel.Mode.VIDEO_EXO_PLAYER,
            PlayerViewModel.Mode.AUDIO_EXO_PLAYER -> {
                exoPlayer.pause()
            }
        }
    }

    private fun onResume() {
        val model = playerViewModel.modelLiveData.value
        if (model.fullscreen &&
            (//model.mode == PlayerViewModel.Mode.YOU_TUBE_PLAYER ||
                    model.mode == PlayerViewModel.Mode.VIDEO_EXO_PLAYER)
        ) {
            onFullscreenMode(true)
        }
    }

    private fun changeFullscreen(fullscreen: Boolean) {
        when (playerViewModel.modelLiveData.value.mode) {
            /*PlayerViewModel.Mode.YOU_TUBE_PLAYER -> {
                (pvYouTubePlayer.parent as? ViewGroup)?.removeView(pvYouTubePlayer)

                if (fullscreen) {
                    lFullscreen.addView(pvYouTubePlayer)
                    youTubePlayerViews.fullscreenButton?.setImageResource(R.drawable.ayp_ic_fullscreen_exit_24dp)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvYouTubePlayer)
                    youTubePlayerViews.fullscreenButton?.setImageResource(R.drawable.ayp_ic_fullscreen_24dp)
                }
                pvYouTubePlayer.enterFullScreen()//Устанавливает максимальный размер по родителю
                lFullscreen.visibility = visibleGone(fullscreen)
                onFullscreenMode(fullscreen)
            }*/
            PlayerViewModel.Mode.VIDEO_EXO_PLAYER -> {
                (pvVideoExoPlayer.parent as? ViewGroup)?.removeView(pvVideoExoPlayer)

                if (fullscreen) {
                    lFullscreen.addView(pvVideoExoPlayer)
                    videoExoPlayerViews.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_exit)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvVideoExoPlayer)
                    videoExoPlayerViews.fullscreenButton.setImageResource(R.drawable.exo_ic_fullscreen_enter)
                }
                //Каждый раз задаём плеер, иначе он не поймёт что только что произошло
                pvVideoExoPlayer.player = exoPlayer
                lFullscreen.visibility = visibleGone(fullscreen)
                onFullscreenMode(fullscreen)
            }
            PlayerViewModel.Mode.AUDIO_EXO_PLAYER -> {
                (pvAudioExoPlayer.parent as? ViewGroup)?.removeView(pvAudioExoPlayer)

                if (fullscreen) {
                    lFullscreen.addView(pvAudioExoPlayer)
                } else {
                    currentMinimizeView?.lVideoMinimized?.addView(pvAudioExoPlayer)
                }
                //Каждый раз задаём плеер, иначе он не поймёт что только что произошло
                pvAudioExoPlayer.player = exoPlayer
                lFullscreen.visibility = visibleGone(fullscreen)
                onFullscreenMode(fullscreen)
                pvAudioExoPlayer.showController()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return playerViewModel.onBackPressed()
    }

    override fun applyPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        playerType: IUsedeskMediaPlayerAdapter.PlayerType,
        doOnApply: () -> Unit,
        doOnCancelPlay: () -> Unit,
        doOnControlsVisibilityChanged: ((Boolean) -> Unit)
    ) {
        hideKeyboard(lFullscreen)

        //Текущий плеер станет старым
        lastMinimizeView = currentMinimizeView

        //Сбросим старое воспроизведение, если оно было
        resetPlayer()

        //Сохраним данные для нового воспроизведения
        currentMinimizeView =
            MinimizeView(lMinimized, doOnCancelPlay, doOnControlsVisibilityChanged)

        when (playerType) {
            IUsedeskMediaPlayerAdapter.PlayerType.VIDEO -> {
                playerViewModel.videoApply(mediaKey)
            }
            IUsedeskMediaPlayerAdapter.PlayerType.AUDIO -> {
                playerViewModel.audioApply(mediaKey)
            }
            /*PlayerType.YOUTUBE -> {
                playerViewModel.youTubeApply(mediaKey)
            }*/
        }

        doOnApply()
    }

    private fun resetPlayer() {
        exoPlayer.stop()
        pvVideoExoPlayer.player = null
        pvAudioExoPlayer.player = null
        (pvVideoExoPlayer.parent as? ViewGroup)?.removeView(pvVideoExoPlayer)
        (pvAudioExoPlayer.parent as? ViewGroup)?.removeView(pvAudioExoPlayer)
        //(pvYouTubePlayer.parent as? ViewGroup)?.removeView(pvYouTubePlayer)
        lastMinimizeView?.release()
        lastMinimizeView = null
        lFullscreen.visibility = View.GONE
    }

    override fun cancelPlayer(key: String) {
        val model = playerViewModel.modelLiveData.value
        if (model.key == key && !model.fullscreen) {
            lastMinimizeView = currentMinimizeView
            resetPlayer()

            playerViewModel.reset()
        }
    }

    /**
     * Вызывается для того, чтобы прикрепиться к адаптеру, для реакции на minimize или переключения
     * плеера
     */
    override fun reapplyPlayer(
        lVideoMinimized: ViewGroup,
        mediaKey: String,
        doOnReapply: () -> Unit,
        doOnCancelPlay: () -> Unit,
        doOnControlsVisibilityChanged: ((Boolean) -> Unit)
    ) {
        val model = playerViewModel.modelLiveData.value
        if (model.key == mediaKey) {
            //Сохраним данные для плеера
            currentMinimizeView = MinimizeView(
                lVideoMinimized,
                doOnCancelPlay,
                doOnControlsVisibilityChanged
            )

            if (model.fullscreen) {
                changeFullscreen(true)
            } else {
                changeFullscreen(false)
            }
            doOnReapply()
        }
    }

    private class VideoExoPlayerViews(exoPlayerView: PlayerView) {
        val fullscreenButton = exoPlayerView.findViewById<ImageView>(R.id.exo_fullscreen_icon)
        val controls = exoPlayerView.findViewById<View>(R.id.exo_controller)
        val pbLoading = exoPlayerView.findViewById<ProgressBar>(R.id.loading)
    }

    private class AudioExoPlayerViews(exoPlayerView: PlayerView) {
        val contentFrame = exoPlayerView.findViewById<View>(R.id.exo_content_frame)
    }

    /*private class YouTubePlayerViews(youTubePlayerView: YouTubePlayerView) {
        val youTubeButton = youTubePlayerView.findViewById<ImageView>(R.id.youtube_button)
        val fullscreenButton = youTubePlayerView.findViewById<ImageView>(R.id.fullscreen_button)
        val progress = youTubePlayerView.findViewById<View>(R.id.progress)
    }*/

    private class MinimizeView(
        val lVideoMinimized: ViewGroup,
        val doOnCancelPlay: () -> Unit,
        val doOnControlsVisibilityChanged: (Boolean) -> Unit
    ) {
        fun release() {
            doOnControlsVisibilityChanged.invoke(false)
            doOnCancelPlay.invoke()
        }
    }
}