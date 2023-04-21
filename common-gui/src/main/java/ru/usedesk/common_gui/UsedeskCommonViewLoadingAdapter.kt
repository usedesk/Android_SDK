
package ru.usedesk.common_gui

import android.graphics.drawable.Animatable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

class UsedeskCommonViewLoadingAdapter(
    private val binding: Binding
) {

    fun update(state: State) {
        when (state) {
            State.LOADING -> {
                binding.rootView.visibility = View.VISIBLE

                binding.imageBinding.ivLoadingImage.setImageResourceSafe(
                    binding.imageBinding.loadingImageId
                )
                binding.titleBinding.tvLoadingTitle.setTextSafe(
                    binding.titleBinding.loadingTitleId
                )
                binding.textBinding.tvLoadingText.setTextSafe(
                    binding.textBinding.loadingTextId
                )
                binding.pbLoading.visibility = View.VISIBLE
            }
            State.RELOADING,
            State.FAILED -> {
                binding.rootView.visibility = View.VISIBLE

                binding.imageBinding.ivLoadingImage.setImageResourceSafe(
                    binding.imageBinding.noInternetImageId
                )
                binding.titleBinding.tvLoadingTitle.setTextSafe(
                    binding.titleBinding.noInternetTitleId
                )
                binding.textBinding.tvLoadingText.setTextSafe(
                    binding.textBinding.noInternetTextId
                )
                binding.pbLoading.visibility = View.GONE
            }
            State.LOADED -> {
                binding.rootView.visibility = View.GONE
            }
        }
    }

    private fun ImageView.setImageResourceSafe(
        resourceId: Int
    ) {
        if (resourceId == 0) {
            visibility = View.GONE
            setImageDrawable(null)
        } else {
            visibility = View.VISIBLE
            setImageResource(resourceId)
            (drawable as? Animatable)?.start()
        }
    }

    private fun TextView.setTextSafe(resourceId: Int) {
        if (resourceId == 0) {
            text = ""
            visibility = View.GONE
        } else {
            setText(resourceId)
            visibility = View.VISIBLE
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val imageBinding = ImageBinding(
            rootView.findViewById(R.id.iv_loading_image),
            styleValues.getStyle(R.attr.usedesk_common_view_loading_image)
        )
        val titleBinding = TitleBinding(
            rootView.findViewById(R.id.tv_loading_title),
            styleValues.getStyle(R.attr.usedesk_common_view_loading_title)
        )
        val textBinding = TextBinding(
            rootView.findViewById(R.id.tv_loading_text),
            styleValues.getStyle(R.attr.usedesk_common_view_loading_text)
        )
        val pbLoading = rootView.findViewById<ProgressBar>(R.id.pb_loading)
    }

    class ImageBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivLoadingImage = rootView as ImageView
        val loadingImageId = styleValues.getIdOrZero(R.attr.usedesk_drawable_1)
        val noInternetImageId = styleValues.getIdOrZero(R.attr.usedesk_drawable_2)
    }

    class TitleBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvLoadingTitle = rootView as TextView
        val loadingTitleId = styleValues.getIdOrZero(R.attr.usedesk_text_1)
        val noInternetTitleId = styleValues.getIdOrZero(R.attr.usedesk_text_2)
    }

    class TextBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvLoadingText = rootView as TextView
        val loadingTextId = styleValues.getIdOrZero(R.attr.usedesk_text_1)
        val noInternetTextId = styleValues.getIdOrZero(R.attr.usedesk_text_2)
    }

    enum class State {
        LOADING,
        RELOADING,
        LOADED,
        FAILED
    }
}