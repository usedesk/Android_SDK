
package ru.usedesk.common_gui

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


fun ImageView.showImage(
    url: String,
    @DrawableRes errorResId: Int = 0,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    if (url.isNotEmpty()) {
        val base = Glide.with(this)
            .load(url)

        when (errorResId) {
            0 -> base
            else -> base.error(errorResId)
        }.listener(
            AppRequestListener(
                onError = onError,
                onSuccess = onSuccess
            )
        ).into(this)
    } else {
        if (errorResId != 0) {
            setImageResource(errorResId)
        }
    }
}

fun ImageView.showImage(
    url: String,
    loadingId: Int = 0,
    vLoading: View? = null,
    vError: View? = null,
    onSuccess: () -> Unit = {},
    onError: () -> Unit = {},
    ignoreCache: Boolean = false,
    oldPlaceholder: Boolean = false
) {
    showImageStatus(vLoading, true, vError, false)

    var glide = Glide.with(context)
        .load(url)
        .listener(AppRequestListener(vLoading, vError, onSuccess, onError))

    glide = when {
        ignoreCache -> glide.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
        else -> glide.diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    glide = when (scaleType) {
        ImageView.ScaleType.FIT_CENTER -> glide.fitCenter()
        else -> glide.centerCrop()
    }

    when {
        loadingId != 0 -> glide.placeholder(loadingId)
        oldPlaceholder -> glide.placeholder(drawable)
        else -> glide
    }.into(this)
}

fun ImageView.clearImage() {
    Glide.with(context).clear(this)
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

    private fun onAction(action: () -> Unit) {
        try {
            action()
        } catch (ignored: Exception) {

        }
    }
}