
package ru.usedesk.sample.model.configuration.entity

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class ConfigurationValidation(
    val chatConfigurationValidation: UsedeskChatConfiguration.Validation,
    val knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration.Validation
)