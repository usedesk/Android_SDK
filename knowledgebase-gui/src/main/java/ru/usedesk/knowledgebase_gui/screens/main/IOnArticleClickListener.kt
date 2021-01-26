package ru.usedesk.knowledgebase_gui.screens.main

internal interface IOnArticleClickListener {
    fun onArticleClick(categoryId: Long,
                       articleId: Long,
                       articleTitle: String)
}