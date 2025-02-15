package ru.usedesk.chat_sdk.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class AdditionalFieldsInteractor @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val additionalFieldsRepository: AdditionalFieldsRepository,
    private val userInfoRepository: UserInfoRepository,
    private val apiRepository: ApiRepository,
) {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initAdditionalFields() {
        if (initConfiguration.additionalFields.isNotEmpty() ||
            initConfiguration.additionalNestedFields.isNotEmpty()
        ) {
            ioScope.launch {
                val needToSendAdditionalFieldsFlow = combine(
                    additionalFieldsRepository.additionalFieldsNeededFlow,
                    additionalFieldsRepository.firstMessageSentFlow,
                ) { additionalFieldsNeeded, firstMessageSent ->
                    additionalFieldsNeeded && firstMessageSent
                }
                combine(
                    userInfoRepository.clientTokenFlowNotNull,
                    needToSendAdditionalFieldsFlow.filter { it },
                ) { clientToken, _ ->
                    additionalFieldsRepository.additionalFieldsSent(true)
                    val response = apiRepository.sendFields(
                        clientToken,
                        initConfiguration,
                        initConfiguration.additionalFields,
                        initConfiguration.additionalNestedFields
                    )
                    if (response is IApiRepository.SendAdditionalFieldsResponse.Error) {
                        delay(REPEAT_DELAY)
                        additionalFieldsRepository.additionalFieldsSent(false)
                    }
                }.collect()
            }
        }
    }

    companion object {
        private const val REPEAT_DELAY = 5000L
    }
}