package ru.usedesk.knowledgebase_sdk.internal.di

import android.content.Context
import ru.usedesk.common_sdk.internal.appdi.InjectBox
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration
import toothpick.ktp.delegate.inject

class InstanceBox(
        appContext: Context, knowledgeBaseConfiguration:
        UsedeskKnowledgeBaseConfiguration
) : InjectBox() {

    val knowledgeBaseSdk: IUsedeskKnowledgeBase by inject()

    init {
        init(MainModule(appContext, knowledgeBaseConfiguration))
    }
}