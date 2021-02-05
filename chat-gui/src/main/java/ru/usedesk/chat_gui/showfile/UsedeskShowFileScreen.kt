package ru.usedesk.chat_gui.showfile

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.*

class UsedeskShowFileScreen : UsedeskFragment() {
    private val viewModel: ShowFileViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var downloadStatusStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var styleValues: UsedeskResourceManager.StyleValues

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_screen_show_file,
                    R.style.Usedesk_Chat_Show_File) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            downloadStatusStyleValues = binding.styleValues
                    .getStyleValues(R.attr.usedesk_chat_show_file_download_status)

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

            styleValues = UsedeskResourceManager.getStyleValues(requireContext(), R.style.Usedesk_Chat_Show_File)

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

            argsGetString(FILE_URL_KEY)?.also { json ->
                val fileUrl = UsedeskFile.deserialize(json)

                viewModel.init(fileUrl)
            }

            hideKeyboard(binding.rootView)
        }

        return binding.rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
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

    private fun onFileUrl(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            if (usedeskFile.isImage()) {
                showInstead(binding.lImage, binding.lFile, true)

                binding.tvTitle.text = usedeskFile.name
                binding.ivImage.setOnClickListener {
                    viewModel.onImageClick()
                }
                showImage(binding.ivImage,
                        0,
                        usedeskFile.content,
                        binding.pbLoading,
                        binding.ivError,
                        { viewModel.onLoaded(true) },
                        { viewModel.onLoaded(false) })
            } else {
                showInstead(binding.lImage, binding.lFile, false)

                binding.tvFileName.text = usedeskFile.name
                binding.tvFileSize.text = usedeskFile.size
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
            UsedeskPermissionUtil.needWriteExternalPermission(binding, this) {
                try {
                    val request = DownloadManager.Request(Uri.parse(usedeskFile.content)).apply {
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, usedeskFile.name)
                        allowScanningByMediaScanner()
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDescription("description")//TODO: это чё такое?
                        setTitle(usedeskFile.name)
                    }

                    val downloadManager = (requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager?)
                    val id = downloadManager?.enqueue(request)
                    if (id != null) {
                        val description = downloadStatusStyleValues.getString(R.attr.usedesk_text_1)
                        Toast.makeText(context, "$description:\n${usedeskFile.name}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val description = downloadStatusStyleValues.getString(R.attr.usedesk_text_2)
                    Toast.makeText(context, "$description:\n${usedeskFile.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val FILE_URL_KEY = "fileUrlKey"

        @JvmStatic
        fun newInstance(usedeskFile: UsedeskFile): UsedeskShowFileScreen {
            return UsedeskShowFileScreen().apply {
                arguments = Bundle().apply {
                    putString(FILE_URL_KEY, usedeskFile.serialize())
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
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