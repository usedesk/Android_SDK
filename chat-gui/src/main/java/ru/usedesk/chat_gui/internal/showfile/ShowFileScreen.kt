package ru.usedesk.chat_gui.internal.showfile

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
import ru.usedesk.chat_gui.databinding.ScreenShowFileBinding
import ru.usedesk.chat_gui.internal._extra.UsedeskFragment
import ru.usedesk.chat_gui.internal._extra.permission.needWriteExternalPermission
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.initAndObserve
import ru.usedesk.common_gui.internal.showImage

class ShowFileScreen : UsedeskFragment() {
    private val viewModel: ShowFileViewModel by viewModels()
    private lateinit var binding: ScreenShowFileBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = inflateItem(inflater,
                R.layout.screen_show_file,
                container)

        val json = requireArguments().getString(FILE_URL_KEY)
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
            if (it == true) {
                binding.lToolbar.visibility = View.VISIBLE
                binding.lBottom.visibility = View.VISIBLE
            } else {
                binding.lToolbar.visibility = View.GONE
                binding.lBottom.visibility = View.GONE
            }
        }
    }

    private fun setBlur(blurView: BlurView) {
        blurView
                .setupWith(binding.lRoot)
                .setFrameClearDrawable(blurView.background)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(16f)
                .setHasFixedTransformationMatrix(true)
    }

    private fun onError(error: Boolean?) {
        if (error == true) {
            binding.ivError.visibility = View.VISIBLE
            binding.ivImage.visibility = View.GONE
        } else {
            binding.ivError.visibility = View.GONE
            binding.ivImage.visibility = View.VISIBLE
        }
    }

    private fun onFileUrl(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            if (usedeskFile.type.startsWith("image")) {
                binding.lImage.visibility = View.VISIBLE
                binding.lFile.visibility = View.GONE

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
                binding.lImage.visibility = View.GONE
                binding.lFile.visibility = View.VISIBLE

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
            needWriteExternalPermission(binding) {
                try {
                    val request = DownloadManager.Request(Uri.parse(usedeskFile.content))
                            .apply {
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

        fun newInstance(usedeskFile: UsedeskFile): ShowFileScreen {
            return ShowFileScreen().apply {
                arguments = Bundle().apply {
                    putString(FILE_URL_KEY, usedeskFile.serialize())
                }
            }
        }
    }
}