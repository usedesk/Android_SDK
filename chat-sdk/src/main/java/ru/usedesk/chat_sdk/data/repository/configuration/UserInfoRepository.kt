
package ru.usedesk.chat_sdk.data.repository.configuration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class UserInfoRepository @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val configurationLoader: IConfigurationLoader
) : IUserInfoRepository {

    private val initKey = initConfiguration.userKey()
    private val mutex = Mutex()
    private val configurationMap = mutableMapOf<String, UsedeskChatConfiguration>()

    init {
        runBlocking {
            mutex.lock(configurationMap)
            CoroutineScope(Dispatchers.IO).launch {
                configurationLoader.getData()?.let { configurations ->
                    configurationMap.putAll(configurations.associateBy { it.userKey() })
                }
                mutex.unlock(configurationMap)
            }
        }
    }

    override fun getConfiguration() = runBlocking {
        mutex.withLock { configurationMap[initKey] }
    }

    override fun updateConfiguration(
        onUpdate: UsedeskChatConfiguration.() -> UsedeskChatConfiguration
    ) {
        runBlocking {
            mutex.withLock {
                configurationMap[initKey] =
                    configurationMap.getOrElse(initKey) { initConfiguration }.onUpdate()
                configurationLoader.setData(configurationMap.values.toTypedArray())
            }
        }
    }
}
