package ru.usedesk.knowledgebase_gui.screens.article

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ru.usedesk.knowledgebase_gui.screens.article.item.ArticleItem
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

class ArticlePagesAdapter(
        private val viewPager: ViewPager,
        fragmentManager: FragmentManager,
        private val onItemSelected: (UsedeskArticleInfo) -> Unit
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var items: List<UsedeskArticleInfo> = listOf()

    init {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                onItemSelected(items[position])
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        viewPager.adapter = this
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Fragment {
        val previous = items.getOrNull(position - 1)
        val current = items[position]
        val next = items.getOrNull(position + 1)
        return ArticleItem.newInstance(current.id,
                previous?.title,
                next?.title)
    }

    fun update(articles: List<UsedeskArticleInfo>,
               selectedArticleId: Long) {
        this.items = articles
        notifyDataSetChanged()
        this.items.indexOfFirst {
            it.id == selectedArticleId
        }.also { index ->
            viewPager.setCurrentItem(index, false)
            onItemSelected(items[index])
        }
    }

    fun onPrevious() {
        viewPager.setCurrentItem(viewPager.currentItem - 1, true)
    }

    fun onNext() {
        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
    }
}