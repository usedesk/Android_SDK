package ru.usedesk.common_gui.internal

import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun setImage(imageImageView: ImageView, pictureUrl: String, errorResId: Int) {
    imageImageView.setImageResource(errorResId)
    if (!TextUtils.isEmpty(pictureUrl)) {
        Glide.with(imageImageView)
                .load(pictureUrl)
                .error(errorResId)
                .into(imageImageView)
    }
}

fun setImageCenter(target: ImageView, uri: Uri, errorId: Int) {
    Glide.with(target)
            .load(uri)
            .centerCrop()
            .error(errorId)
            .into(target)
}

fun setImage(target: ImageView, path: String?) {
    if (path != null) {
        Glide.with(target)
                .load(path)
                .into(target)
    } else {
        target.setImageDrawable(null)
    }
}


fun showImage(ivTarget: ImageView,
              loadingId: Int,
              url: String,
              vLoading: View?,
              vError: View?,
              onSuccess: () -> Unit = {},
              onError: () -> Unit = {}) {
    showImageStatus(vLoading, true, vError, false)

    var glide = Glide.with(ivTarget)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

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

private fun showImageStatus(vLoading: View?,
                            loadingShow: Boolean,
                            vError: View?,
                            errorShow: Boolean) {
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

class AppRequestListener(
        private val vLoading: View? = null,
        private val vError: View? = null,
        private val onSuccess: () -> Unit = {},
        private val onError: () -> Unit = {}
) : RequestListener<Drawable?> {

    override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>?,
            isFirstResource: Boolean
    ): Boolean {
        showImageStatus(vLoading, false, vError, true)
        onAction(onError)
        return false
    }

    override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable?>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
    ): Boolean {
        showImageStatus(vLoading, false, vError, false)
        onAction(onSuccess)
        return false
    }
}