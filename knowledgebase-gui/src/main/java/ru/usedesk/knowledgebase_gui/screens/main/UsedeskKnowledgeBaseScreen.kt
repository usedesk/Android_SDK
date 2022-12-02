package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.*
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screens.ToolbarSearchAdapter
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var toolbarDefaultAdapter: UsedeskToolbarAdapter
    private lateinit var toolbarSearchAdapter: ToolbarSearchAdapter
    private lateinit var navController: NavController

    private var fabDefaultBottomMargin = 0

    internal var withArticleRating = true
        private set

    private var fabAnimation: FabAnimation? = null

    private lateinit var sectionsTitle: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_screen_knowledge_base,
            R.style.Usedesk_KnowledgeBase_Screen,
            ::Binding
        )

        sectionsTitle = binding.styleValues
            .getStyleValues(R.attr.usedesk_common_toolbar)
            .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
            .getString(R.attr.usedesk_text_1)

        fabDefaultBottomMargin = binding.styleValues.getStyleValues(
            R.attr.usedesk_knowledgebase_screen_support_button
        ).getFloat(android.R.attr.layout_marginBottom).toInt()

        navController = (childFragmentManager.findFragmentById(R.id.page_container)
                as NavHostFragment).navController

        withArticleRating = argsGetBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
        val withSupportButton = argsGetBoolean(WITH_SUPPORT_BUTTON_KEY, true)

        binding.fabSupport.run {
            visibility = visibleGone(withSupportButton)
            setOnClickListener {
                findParent<IUsedeskOnSupportClickListener>()?.onSupportClick()
            }
        }

        toolbarDefaultAdapter = UsedeskToolbarAdapter(binding.toolbar).apply {
            setBackButton(requireActivity()::onBackPressed)

            setActionButton {
                navController.apply {
                    if (currentDestination?.id != R.id.dest_articlesSearchPage) {
                        navigate(R.id.dest_articlesSearchPage)
                    }
                }
            }
        }

        toolbarSearchAdapter = ToolbarSearchAdapter(
            binding.toolbarSearch, { query ->
                if (query.isNotEmpty()) {
                    viewModel.onSearchQuery(query)
                }
            },
            this@UsedeskKnowledgeBaseScreen::onBackPressed
        )

        hideKeyboard(binding.rootView)

        navController.addOnDestinationChangedListener { _, destination, args ->
            when (destination.id) {
                R.id.dest_sectionsPage,
                R.id.dest_categoriesPage,
                R.id.dest_articlesPage,
                R.id.dest_articlePage -> {
                    toolbarDefaultAdapter.show()
                    toolbarSearchAdapter.hide()
                    toolbarDefaultAdapter.setTitle(
                        args?.getString(COMMON_TITLE_KEY)
                            ?: sectionsTitle
                    )
                }
                R.id.dest_articlesSearchPage -> {
                    toolbarDefaultAdapter.hide()
                    toolbarSearchAdapter.show()
                }
            }
            onSupportButtonBottomMargin(0)
        }

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init {
            argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KNOWLEDGE_BASE_CONFIGURATION)
                ?.let(UsedeskKnowledgeBaseSdk::setConfiguration)
            UsedeskKnowledgeBaseSdk.init(requireContext())
        }
    }

    internal fun onTitle(title: String) {
        toolbarDefaultAdapter.setTitle(title)
    }

    internal fun onSupportButtonBottomMargin(bottomMargin: Int) {
        val newBottomMargin = fabDefaultBottomMargin + bottomMargin
        if (fabAnimation?.newBottomMargin != newBottomMargin) {
            binding.fabSupport.clearAnimation()
            fabAnimation = FabAnimation(
                binding.fabSupport,
                newBottomMargin
            )
            binding.fabSupport.startAnimation(fabAnimation)
        }
    }

    override fun onBackPressed(): Boolean {
        return navController.popBackStack()
    }

    class FabAnimation(
        val view: View,
        val newBottomMargin: Int
    ) : Animation() {
        private val oldBottomMargin = view.marginBottom

        init {
            duration = 300
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val dif = newBottomMargin - oldBottomMargin
            val currentMargin = oldBottomMargin + (dif * interpolatedTime).toInt()
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = currentMargin)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
        val toolbarSearch =
            ToolbarSearchAdapter.Binding(rootView.findViewById(R.id.toolbar_search), defaultStyleId)
        val fabSupport = rootView.findViewById<FloatingActionButton>(R.id.fab_support)
    }

    companion object {
        internal const val COMMON_TITLE_KEY = "commonTitleKey"
        private const val WITH_SUPPORT_BUTTON_KEY = "withSupportButtonKey"
        private const val WITH_ARTICLE_RATING_KEY = "withArticleRatingKey"
        private const val KNOWLEDGE_BASE_CONFIGURATION = "knowledgeBaseConfiguration"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration? = null
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                withSupportButton,
                withArticleRating,
                knowledgeBaseConfiguration
            )
        }

        @JvmStatic
        @JvmOverloads
        fun createBundle(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration? = null
        ): Bundle = Bundle().apply {
            putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
            putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
            knowledgeBaseConfiguration?.let {
                putParcelable(KNOWLEDGE_BASE_CONFIGURATION, it)
            }
        }
    }
}