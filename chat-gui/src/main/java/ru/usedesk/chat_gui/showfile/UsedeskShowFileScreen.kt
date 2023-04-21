
package ru.usedesk.chat_gui.showfile

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.hideKeyboard
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showImage
import ru.usedesk.common_gui.showInstead
import ru.usedesk.common_gui.visibleGone

class UsedeskShowFileScreen : UsedeskFragment() {
    private val viewModel: ShowFileViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var downloadStatusStyleValues: UsedeskResourceManager.StyleValues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateItem(
        inflater,
        container,
        R.layout.usedesk_screen_show_file,
        R.style.Usedesk_Chat_Show_File,
        ::Binding
    ).apply {
        binding = this

        downloadStatusStyleValues = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_show_file_download_status_toast)

        binding.ivBack.setOnClickListener { requireActivity().onBackPressed() }

        binding.ivShare.setOnClickListener { onShareFile(viewModel.modelFlow.value.file) }

        binding.ivDownload.setOnClickListener {
            viewModel.modelFlow.value.file?.let { file ->
                findParent<IUsedeskOnDownloadListener>()?.onDownload(
                    file.content,
                    file.name
                )
            }
        }

        binding.ivError.setOnClickListener { viewModel.onRetryPreview() }

        binding.lToolbar.setBlur()
        binding.lBottom.setBlur()
    }.rootView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideKeyboard(binding.rootView)

        argsGetParcelable<UsedeskFile>(FILE_KEY)?.let(viewModel::setFile)

        viewModel.modelFlow.onEachWithOld { old, new ->
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
                        binding.ivImage.showImage(
                            new.file.content,
                            vLoading = binding.pbLoading,
                            vError = binding.ivError,
                            onSuccess = { viewModel.onLoaded(true) },
                            onError = { viewModel.onLoaded(false) }
                        )
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
    }

    private fun BlurView.setBlur() {
        setupWith(binding.rootView as ViewGroup, RenderScriptBlur(context))
            .setFrameClearDrawable(background)
            .setBlurRadius(16f)
    }

    private fun onError(error: Boolean?) {
        showInstead(binding.ivError, binding.ivImage, error == true)
    }

    private fun onShareFile(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = usedeskFile.type
                val uri = Uri.parse(usedeskFile.content)
                when (uri.scheme) {
                    ContentResolver.SCHEME_FILE,
                    ContentResolver.SCHEME_CONTENT -> {
                        val providerUri = uri.toProviderUri()
                        clipData = ClipData.newRawUri("", providerUri)
                        putExtra(Intent.EXTRA_STREAM, providerUri)
                        setDataAndType(providerUri, usedeskFile.type)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    else -> putExtra(Intent.EXTRA_TEXT, usedeskFile.content)
                }
            }
            val chooserShareIntent = Intent.createChooser(shareIntent, null)
            startActivity(chooserShareIntent)
        }
    }

    companion object {
        private const val FILE_KEY = "fileKey"

        @JvmStatic
        fun newInstance(usedeskFile: UsedeskFile) = UsedeskShowFileScreen().apply {
            arguments = createBundle(usedeskFile)
        }

        @JvmStatic
        fun createBundle(usedeskFile: UsedeskFile) = Bundle().apply {
            putParcelable(FILE_KEY, usedeskFile)
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