package ru.usedesk.chat_gui.external.showfile

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskScreenShowFileBinding
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_gui.internal.*

class UsedeskShowFileFragment : UsedeskFragment(R.style.Usedesk_Theme_Chat) {
    private val viewModel: ShowFileViewModel by viewModels()

    private lateinit var binding: UsedeskScreenShowFileBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val json = argsGetString(FILE_URL_KEY)

        binding = inflateBinding(inflater,
                container,
                R.layout.usedesk_screen_show_file,
                defaultStyleId)

        if (json != null) {
            val fileUrl = UsedeskFile.deserialize(json)

            viewModel.init(fileUrl)
        }

        init()

        return binding.root
    }

    private fun init() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivShare.setOnClickListener {
            onShareFile(viewModel.fileUrlLiveData.value)
        }

        binding.ivDownload.setOnClickListener {
            onDownloadFile(viewModel.fileUrlLiveData.value)
        }

        setBlur(binding.lToolbar)
        setBlur(binding.lBottom)

        initAndObserve(viewLifecycleOwner, viewModel.fileUrlLiveData) {
            onFileUrl(it)
        }

        initAndObserve(viewLifecycleOwner, viewModel.errorLiveData) {
            onError(it)
        }

        initAndObserve(viewLifecycleOwner, viewModel.panelShowLiveData) {
            binding.lToolbar.visibility = visibleGone(it == true)
            binding.lBottom.visibility = visibleGone(it == true)
        }
    }

    private fun setBlur(blurView: BlurView) {
        blurView.setupWith(binding.lRoot)
                .setFrameClearDrawable(blurView.background)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(16f)
                .setHasFixedTransformationMatrix(true)
    }

    private fun onError(error: Boolean?) {
        showInstead(binding.ivError, binding.ivImage, error == true)
    }

    private fun onFileUrl(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            if (usedeskFile.isImage()) {
                showInstead(binding.lImage, binding.lFile, true)

                binding.tvTitle.text = usedeskFile.name
                binding.ivImage.setOnClickListener {
                    viewModel.onImageClick()
                }
                showImage(binding.ivImage,
                        R.drawable.ic_image_loading,
                        usedeskFile.content,
                        binding.pbLoading,
                        binding.ivError,
                        { viewModel.onLoaded(true) },
                        { viewModel.onLoaded(false) })
            } else {
                showInstead(binding.lImage, binding.lFile, false)

                binding.tvFileName.text = usedeskFile.name
                binding.tvFileSize.text = usedeskFile.size//formatSize(binding.root.context, usedeskFile.size)
                viewModel.onLoaded(true)
            }
        }
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

    private fun onDownloadFile(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            PermissionUtil.needWriteExternalPermission(binding.root,
                    R.string.need_permission,
                    R.string.settings) {
                try {
                    val request = DownloadManager.Request(Uri.parse(usedeskFile.content)).apply {
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "")
                        allowScanningByMediaScanner()
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDescription("description")
                        setTitle(usedeskFile.name)
                    }

                    val downloadManager = (requireActivity()
                            .getSystemService(DOWNLOAD_SERVICE) as DownloadManager?)
                    val id = downloadManager?.enqueue(request)
                    if (id != null) {
                        val description = resources.getString(R.string.download_started)
                        Toast.makeText(context, "$description:\n${usedeskFile.name}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val description = resources.getString(R.string.download_failed)
                    Toast.makeText(context, "$description:\n${usedeskFile.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val FILE_URL_KEY = "fileUrlKey"

        @JvmStatic
        fun newInstance(usedeskFile: UsedeskFile): UsedeskShowFileFragment {
            return UsedeskShowFileFragment().apply {
                arguments = Bundle().apply {
                    putString(FILE_URL_KEY, usedeskFile.serialize())
                }
            }
        }
    }
}