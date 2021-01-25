package ru.usedesk.knowledgebase_gui.screens.articles

internal interface IOnArticleClickListener {
    fun onArticleClick(categoryId: Long, articleId: Long)
}