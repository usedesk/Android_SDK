package ru.usedesk.chat_gui.showfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.*

class UsedeskShowFileScreen : UsedeskFragment() {
    private val viewModel: ShowFileViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var downloadStatusStyleValues: UsedeskResourceManager.StyleValues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_screen_show_file,
            R.style.Usedesk_Chat_Show_File
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        downloadStatusStyleValues = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_show_file_download_status_toast)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.ivShare.setOnClickListener {
            onShareFile(viewModel.modelLiveData.value.file)
        }

        binding.ivDownload.setOnClickListener {
            viewModel.modelLiveData.value.file?.let { file ->
                needWriteExternalPermission(this) {
                    getParentListener<IUsedeskOnDownloadListener>()?.onDownload(
                        file.content,
                        file.name
                    )
                }
            }
        }

        binding.ivError.setOnClickListener {
            viewModel.onRetryPreview()
        }

        setBlur(binding.lToolbar)
        setBlur(binding.lBottom)

        argsGetObject(Keys.FILE.name, UsedeskFile::class.java)?.let { file ->
            viewModel.init(file)
        }

        hideKeyboard(binding.rootView)

        viewModel.modelLiveData.initAndObserveWithOld(viewLifecycleOwner) { old, new ->
            if (old?.file != new.file ||
                old?.error != new.error && !new.error
            ) {
                if (new.file != null) {
                    if (new.file.isImage()) {
                        showInstead(binding.lImage, binding.lFile, true)

                        binding.tvTitle.text = new.file.name
                        binding.ivImage.setOnClickListener {
                            viewModel.onImageClick()
                        }
                        showImage(binding.ivImage,
                            0,
                            new.file.content,
                            binding.pbLoading,
                            binding.ivError,
                            { viewModel.onLoaded(true) },
                            { viewModel.onLoaded(false) })
                    } else {
                        showInstead(binding.lImage, binding.lFile, false)

                        binding.tvFileName.text = new.file.name
                        binding.tvFileSize.text = new.file.size
                        viewModel.onLoaded(true)
                    }
                }
            }
            if (old?.error != new.error) {
                onError(new.error)
            }
            if (old?.panelShow != new.panelShow) {
                binding.lToolbar.visibility = visibleGone(new.panelShow)
                binding.lBottom.visibility = visibleGone(new.panelShow)
            }
        }

        return binding.rootView
    }

    private fun setBlur(blurView: BlurView) {
        blurView.setupWith(binding.rootView as ViewGroup)
            .setFrameClearDrawable(blurView.background)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(16f)
            .setHasFixedTransformationMatrix(true)
    }

    private fun onError(error: Boolean?) {
        showInstead(binding.ivError, binding.ivImage, error == true)
    }

    private fun onShareFile(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            ShareCompat.IntentBuilder
                .from(requireActivity())
                .setType(usedeskFile.type)
                .setText(usedeskFile.content)
                .startChooser()
        }
    }

    companion object {
        private enum class Keys {
            FILE
        }

        @JvmStatic
        fun newInstance(usedeskFile: UsedeskFile): UsedeskShowFileScreen {
            return UsedeskShowFileScreen().apply {
                arguments = Bundle().apply {
                    argsPutObject(this, Keys.FILE.name, usedeskFile)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lToolbar: BlurView = rootView.findViewById(R.id.l_toolbar)
        val lBottom: BlurView = rootView.findViewById(R.id.l_bottom)
        val lImage: ViewGroup = rootView.findViewById(R.id.l_image)
        val lFile: ViewGroup = rootView.findViewById(R.id.l_file)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val ivBack: ImageView = rootView.findViewById(R.id.iv_back)
        val ivShare: ImageView = rootView.findViewById(R.id.iv_share)
        val ivError: ImageView = rootView.findViewById(R.id.iv_error)
        val ivImage: ImageView = rootView.findViewById(R.id.iv_image)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvFileName: TextView = rootView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = rootView.findViewById(R.id.tv_file_size)
        val ivDownload: ImageView = rootView.findViewById(R.id.iv_download)
    }
}