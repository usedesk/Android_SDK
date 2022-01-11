package ru.usedesk.common_gui

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun setImage(
    imageImageView: ImageView,
    pictureUrl: String,
    errorResId: Int,
    onError: (() -> Unit)? = null,
    onSuccess: (() -> Unit)? = null
) {
    imageImageView.setImageResource(errorResId)
    if (!TextUtils.isEmpty(pictureUrl)) {
        Glide.with(imageImageView.context)
            .load(pictureUrl)
            .error(errorResId)
            .listener(AppRequestListener(onError = {
                onError?.invoke()
            }, onSuccess = {
                onSuccess?.invoke()
            }))
            .into(imageImageView)
    }
}

fun showImage(
    ivTarget: ImageView,
    loadingId: Int,
    url: String,
    vLoading: View? = null,
    vError: View? = null,
    onSuccess: () -> Unit = {},
    onError: () -> Unit = {},
    ignoreCache: Boolean = false
) {
    showImageStatus(vLoading, true, vError, false)

    var glide = Glide.with(ivTarget.context)
        .load(url)

    glide = if (ignoreCache) {
        glide.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
    } else {
        glide.diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    glide = if (ivTarget.scaleType == ImageView.ScaleType.FIT_CENTER) {
        glide.fitCenter()
    } else {
        glide.centerCrop()
    }

    glide.listener(AppRequestListener(vLoading, vError, onSuccess, onError))
        .apply {
            if (loadingId != 0) {
                placeholder(loadingId)
            }
        }
        .into(ivTarget)
}

fun clearImage(ivTarget: ImageView) {
    Glide.with(ivTarget.context.applicationContext)
        .clear(ivTarget)
}

private fun showImageStatus(
    vLoading: View?,
    loadingShow: Boolean,
    vError: View?,
    errorShow: Boolean
) {
    if (vLoading != null) {
        vLoading.visibility = visibleGone(loadingShow)
    }
    if (vError != null) {
        vError.visibility = visibleGone(errorShow)
    }
}

private fun onAction(action: () -> Unit) {
    try {
        action()
    } catch (ignored: Exception) {

    }
}

internal class AppRequestListener<T>(
    private val vLoading: View? = null,
    private val vError: View? = null,
    private val onSuccess: () -> Unit = {},
    private val onError: () -> Unit = {}
) : RequestListener<T?> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<T?>?,
        isFirstResource: Boolean,
    ): Boolean {
        showImageStatus(vLoading, false, vError, true)
        onAction(onError)
        return false
    }

    override fun onResourceReady(
        resource: T?,
        model: Any?,
        target: Target<T?>?,
        dataSource: DataSource?,
        isFirstResource: Boolean,
    ): Boolean {
        showImageStatus(vLoading, false, vError, false)
        onAction(onSuccess)
        return false
    }
}