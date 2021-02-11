package ru.usedesk.knowledgebase_gui.screens.sections

internal interface IOnSectionClickListener {
    fun onSectionClick(sectionId: Long,
                       sectionTitle: String)
}