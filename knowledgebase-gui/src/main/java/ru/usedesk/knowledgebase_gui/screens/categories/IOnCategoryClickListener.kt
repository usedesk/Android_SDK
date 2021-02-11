package ru.usedesk.knowledgebase_gui.screens.categories

internal interface IOnCategoryClickListener {
    fun onCategoryClick(categoryId: Long,
                        articleTitle: String)
}