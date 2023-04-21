
package ru.usedesk.chat_sdk.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.di.IRelease
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class PreparationInteractor @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val apiRepository: IApiRepository,
    private val userInfoRepository: IUserInfoRepository
) : IUsedeskPreparation, IRelease {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun createChat(
        apiToken: String,
        onResult: (IUsedeskPreparation.CreateChatResult) -> Unit
    ) {
        ioScope.launch {
            val response = apiRepository.initChat(
                initConfiguration,
                apiToken
            )
            val result = when (response) {
                is IApiRepository.InitChatResponse.ApiError -> IUsedeskPreparation.CreateChatResult.Error
                is IApiRepository.InitChatResponse.Done -> {
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