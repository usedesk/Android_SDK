package ru.usedesk.knowledgebase_gui.internal.screens.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import ru.usedesk.common_gui.internal.argsGetInt
import ru.usedesk.common_gui.internal.inflateFragment
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage

abstract class FragmentDataView<DATA>(
        private val layoutId: Int,
        private val defaultThemeId: Int
) : Fragment() {
    private lateinit var textViewMessage: TextView
    private lateinit var rootView: ViewGroup
    private lateinit var container: View

    protected abstract fun setDataView(data: DATA)

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val themeId = argsGetInt(arguments, THEME_ID_KEY, defaultThemeId)
        rootView = inflateFragment(inflater, container, themeId, layoutId)

        onView(rootView)

        init()

        getLiveData().observe(viewLifecycleOwner, {
            this.onData(it)
        })

        return rootView
    }

    open fun init() {}

    abstract fun getLiveData(): LiveData<DataOrMessage<DATA>>

    protected open fun onView(view: View) {
        textViewMessage = view.findViewById(R.id.tv_message)
        container = view.findViewById(R.id.container)
    }

    private fun onData(dataOrMessage: DataOrMessage<DATA>) {
        when (dataOrMessage.message) {
            DataOrMessage.Message.LOADING -> onMessage(R.string.loading_title)
            DataOrMessage.Message.ERROR -> onMessage(R.string.loading_error)
            else -> onData(dataOrMessage.data)
        }
    }

    private fun onData(data: DATA) {
        setDataView(data)
        textViewMessage.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    private fun onMessage(resourceId: Int) {
        textViewMessage.setText(resourceId)
        textViewMessage.visibility = View.VISIBLE
        container.visibility = View.GONE
    }

    companion object {
        internal const val THEME_ID_KEY = "themeIdKey"
    }
}