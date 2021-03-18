package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.common_sdk.di.UsedeskInjectBox
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import toothpick.ktp.delegate.inject

internal class InstanceBoxUsedesk(
        context: Context, knowledgeBaseConfiguration:
        UsedeskKnowledgeBaseConfiguration
) : UsedeskInjectBox() {

    val knowledgeBaseSdk: IUsedeskKnowledgeBase by inject()

    init {
        val appContext = context.applicationContext
        init(UsedeskCommonModule(appContext), MainModule(knowledgeBaseConfiguration))
    }
}