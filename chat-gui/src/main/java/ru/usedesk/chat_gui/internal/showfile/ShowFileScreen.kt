package ru.usedesk.chat_gui.internal.showfile

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
import ru.usedesk.chat_gui.external.UsedeskChatFragment.Companion.THEME_ID_KEY
import ru.usedesk.chat_gui.internal._extra.UsedeskFragment
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_gui.internal.*

class ShowFileScreen : UsedeskFragment() {
    private val viewModel: ShowFileViewModel by viewModels()

    private lateinit var rootView: ViewGroup
    private lateinit var lToolbar: BlurView
    private lateinit var lBottom: BlurView
    private lateinit var lFile: View
    private lateinit var lImage: View
    private lateinit var ivError: ImageView
    private lateinit var ivImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvFileName: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var pbLoading: ProgressBar

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val themeId = argsGetInt(arguments, THEME_ID_KEY, R.style.Usedesk_Theme_Chat)
        val json = argsGetString(arguments, FILE_URL_KEY)

        rootView = inflateFragment(inflater, container, themeId, R.layout.usedesk_screen_show_file)

        if (json != null) {
            val fileUrl = UsedeskFile.deserialize(json)

            viewModel.init(fileUrl)
        }

        init()

        return rootView
    }

    private fun init() {
        lToolbar = rootView.findViewById(R.id.l_toolbar)
        lBottom = rootView.findViewById(R.id.l_bottom)
        lFile = rootView.findViewById(R.id.l_file)
        lImage = rootView.findViewById(R.id.l_image)
        ivError = rootView.findViewById(R.id.iv_error)
        ivImage = rootView.findViewById(R.id.iv_image)
        tvTitle = rootView.findViewById(R.id.tv_title)
        tvFileName = rootView.findViewById(R.id.tv_file_name)
        tvFileSize = rootView.findViewById(R.id.tv_file_size)
        pbLoading = rootView.findViewById(R.id.pb_loading)

        rootView.findViewById<View>(R.id.iv_back).setOnClickListener {
            onBackPressed()
        }

        rootView.findViewById<View>(R.id.iv_share).setOnClickListener {
            onShareFile(viewModel.fileUrlLiveData.value)
        }

        rootView.findViewById<View>(R.id.iv_download).setOnClickListener {
            onDownloadFile(viewModel.fileUrlLiveData.value)
        }

        setBlur(lToolbar)
        setBlur(lBottom)

        initAndObserve(viewLifecycleOwner, viewModel.fileUrlLiveData) {
            onFileUrl(it)
        }

        initAndObserve(viewLifecycleOwner, viewModel.errorLiveData) {
            onError(it)
        }

        initAndObserve(viewLifecycleOwner, viewModel.panelShowLiveData) {
            lToolbar.visibility = visibleGone(it == true)
            lBottom.visibility = visibleGone(it == true)
        }
    }

    private fun setBlur(blurView: BlurView) {
        blurView.setupWith(rootView)
                .setFrameClearDrawable(blurView.background)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(16f)
                .setHasFixedTransformationMatrix(true)
    }

    private fun onError(error: Boolean?) {
        showInstead(ivError, ivImage, error == true)
    }

    private fun onFileUrl(usedeskFile: UsedeskFile?) {
        if (usedeskFile != null) {
            if (usedeskFile.type.startsWith("image")) {
                showInstead(lImage, lFile, true)

                tvTitle.text = usedeskFile.name
                ivImage.setOnClickListener {
                    viewModel.onImageClick()
                }
                showImage(ivImage,
                        R.drawable.ic_image_loading,
                        usedeskFile.content,
                        pbLoading,
                        ivError,
                        { viewModel.onLoaded(true) },
                        { viewModel.onLoaded(false) })
            } else {
                showInstead(lImage, lFile, false)

                tvFileName.text = usedeskFile.name
                tvFileSize.text = usedeskFile.size//formatSize(binding.root.context, usedeskFile.size)
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
            PermissionUtil.needWriteExternalPermission(rootView,
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

        @JvmOverloads
        fun newInstance(themeId: Int? = null, usedeskFile: UsedeskFile): ShowFileScreen {
            return ShowFileScreen().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                    putString(FILE_URL_KEY, usedeskFile.serialize())
                }
            }
        }
    }
}