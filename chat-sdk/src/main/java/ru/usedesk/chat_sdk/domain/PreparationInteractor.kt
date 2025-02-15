package ru.usedesk.chat_sdk.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.data.repository.api.ChatApi
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.di.IRelease
import javax.inject.Inject

internal class PreparationInteractor @Inject constructor(
    private val apiRepository: ChatApi,
    private val userInfoRepository: UserInfoRepository
) : IUsedeskPreparation, IRelease {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun createChat(
        apiToken: String,
        onResult: (IUsedeskPreparation.CreateChatResult) -> Unit
    ) {
        ioScope.launch {
            val configuration = userInfoRepository.getConfiguration()
            val response = apiRepository.initChat(
                configuration,
                apiToken
            )
            val result = when (response) {
                is ChatApi.InitChatResponse.ApiError -> IUsedeskPreparation.CreateChatResult.Error
                is ChatApi.InitChatResponse.Done -> {
                    userInfoRepository.updateConfiguration { copy(clientToken = clientToken) }
                    IUsedeskPreparation.CreateChatResult.Done(response.clientToken)
                }
            }
            onResult(result)
        }
    }

    override fun release() {
        ioScope.cancel()
    }
}