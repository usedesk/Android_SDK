package ru.usedesk.chat_gui.chat.loading

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class LoadingPage : UsedeskFragment() {

    private val viewModel: LoadingViewModel by viewModels()

    private lateinit var binding: Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_loading,
            R.style.Usedesk_Chat_Screen_Loading_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_loading_image)

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.modelLiveData.initAndObserveWithOld(viewLifecycleOwner) { old, new ->
            if (old?.state != new.state) {
                when (new.state) {
                    LoadingViewModel.State.LOADING -> {
                        binding.imageBinding.ivLoadingImage.setImageResourceSafe(
                            binding.imageBinding.loadingImageId
                        )
                        binding.titleBinding.tvLoadingTitle.setTextSafe(
                            binding.titleBinding.loadingTitleId
                        )
                        binding.textBinding.tvLoadingText.setTextSafe(
                            binding.textBinding.loadingTextId
                        )
                    }
                    LoadingViewModel.State.NO_INTERNET -> {
                        binding.imageBinding.ivLoadingImage.setImageResourceSafe(
                            binding.imageBinding.noInternetImageId
                        )
                        binding.titleBinding.tvLoadingTitle.setTextSafe(
                            binding.titleBinding.noInternetTitleId
                        )
                        binding.textBinding.tvLoadingText.setTextSafe(
                            binding.textBinding.noInternetTextId
                        )
                    }
                }
            }
            new.goNext.process { page ->
                when (page) {
                    LoadingViewModel.Page.OFFLINE_FORM -> {
                        findNavController().navigate(R.id.action_loadingPage_to_offlineFormPage)
                    }
                    LoadingViewModel.Page.MESSAGES -> {
                        findNavController().navigate(R.id.action_loadingPage_to_messagesPage)
                    }
                }
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
        } else {
            setText(resourceId)
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val imageBinding = ImageBinding(
            rootView.findViewById(R.id.iv_loading_image),
            styleValues.getStyle(R.attr.usedesk_chat_screen_loading_image)
        )
        val titleBinding = TitleBinding(
            rootView.findViewById(R.id.tv_loading_title),
            styleValues.getStyle(R.attr.usedesk_chat_screen_loading_title)
        )
        val textBinding = TextBinding(
            rootView.findViewById(R.id.tv_loading_text),
            styleValues.getStyle(R.attr.usedesk_chat_screen_loading_text)
        )
    }

    internal class ImageBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivLoadingImage = rootView as ImageView
        val loadingImageId = styleValues.getIdOrZero(R.attr.usedesk_drawable_1)
        val noInternetImageId = styleValues.getIdOrZero(R.attr.usedesk_drawable_2)
    }

    internal class TitleBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvLoadingTitle = rootView as TextView
        val loadingTitleId = styleValues.getIdOrZero(R.attr.usedesk_text_1)
        val noInternetTitleId = styleValues.getIdOrZero(R.attr.usedesk_text_2)
    }

    internal class TextBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvLoadingText = rootView as TextView
        val loadingTextId = styleValues.getIdOrZero(R.attr.usedesk_text_1)
        val noInternetTextId = styleValues.getIdOrZero(R.attr.usedesk_text_2)
    }
}