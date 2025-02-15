package ru.usedesk.chat_sdk.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.data.repository.api.ChatApi
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import javax.inject.Inject

internal class AdditionalFieldsInteractor @Inject constructor(
    private val additionalFieldsRepository: AdditionalFieldsRepository,
    private val userInfoRepository: UserInfoRepository,
    private val chatApi: ChatApi,
) {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initAdditionalFields() {
        val configuration = userInfoRepository.getConfiguration()
        if (configuration.additionalFields.isNotEmpty() ||
            configuration.additionalNestedFields.isNotEmpty()
        ) {
            ioScope.launch {
                launchAdditionalFieldsJob()
            }
        }
    }

    private suspend fun launchAdditionalFieldsJob() {
        val needToSendAdditionalFieldsFlow = combine(
            additionalFieldsRepository.additionalFieldsNeededFlow,
            additionalFieldsRepository.firstMessageSentFlow,
        ) { additionalFieldsNeeded, firstMessageSent ->
            additionalFieldsNeeded && firstMessageSent
        }
        combine(
            userInfoRepository.configurationFlow.filterNot { it.clientToken.isNullOrEmpty() },
            needToSendAdditionalFieldsFlow.filter { it },
        ) { configuration, _ ->
            additionalFieldsRepository.additionalFieldsSent(true)
            val response = chatApi.sendFields(configuration)
            if (response is ChatApi.SendAdditionalFieldsResponse.Error) {
                delay(REPEAT_DELAY)
                additionalFieldsRepository.additionalFieldsSent(false)
            }
        }.collect()
    }

    companion object {
        private const val REPEAT_DELAY = 5000L
    }
}