package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import ru.usedesk.common_sdk.di.InjectBox
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import toothpick.ktp.delegate.inject

internal class InstanceBox(
        appContext: Context, knowledgeBaseConfiguration:
        UsedeskKnowledgeBaseConfiguration
) : InjectBox() {

    val knowledgeBaseSdk: IUsedeskKnowledgeBase by inject()

    init {
        init(MainModule(appContext, knowledgeBaseConfiguration))
    }
}