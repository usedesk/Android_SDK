package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import javax.inject.Inject

internal class InjectBoxUsedesk(
    context: Context, knowledgeBaseConfiguration:
    UsedeskKnowledgeBaseConfiguration
) {

    @Inject
    lateinit var knowledgeBaseSdk: IUsedeskKnowledgeBase

    init {

    }

    fun release() {


    }
}