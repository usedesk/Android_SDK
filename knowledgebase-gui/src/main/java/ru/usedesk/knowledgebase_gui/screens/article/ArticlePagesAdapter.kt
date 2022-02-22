package ru.usedesk.knowledgebase_gui.screens.article

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import ru.usedesk.knowledgebase_gui.screens.article.item.ArticleItem
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlePagesAdapter(
    private val viewPager: ViewPager,
    fragmentManager: FragmentManager,
    private val viewModel: ArticlePageViewModel,
    lifecycleOwner: LifecycleOwner,
    private val withArticleRating: Boolean
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var items: List<UsedeskArticleInfo> = listOf()

    init {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                viewModel.onSelect(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        viewPager.adapter = this

        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            if (old?.articles != new.articles) {
                items = new.articles

                notifyDataSetChanged()
            }
            if (viewPager.currentItem != new.selectedPosition) {
                viewPager.setCurrentItem(new.selectedPosition, false)
            }
        }
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Fragment {
        val previous = items.getOrNull(position - 1)
        val current = items[position]
        val next = items.getOrNull(position + 1)
        return ArticleItem.newInstance(
            withArticleRating,
            current.id,
            previous?.title,
            next?.title
        )
    }

    fun onPrevious() {
        val position = viewPager.currentItem - 1
        viewPager.setCurrentItem(position, true)
        viewModel.onSelect(position)
    }

    fun onNext() {
        val position = viewPager.currentItem + 1
        viewPager.setCurrentItem(position, true)
    }
}