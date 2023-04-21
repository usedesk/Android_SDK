
package ru.usedesk.sample

import android.content.Context
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository

class ServiceLocator(appContext: Context) {
    val configurationRepository: ConfigurationRepository

    init {
        val sharedPreferences = appContext.getSharedPreferences(
            "SampleConfiguration",
            Context.MODE_PRIVATE
        )
        configurationRepository = ConfigurationRepository(sharedPreferences)
    }

    companion object {
        lateinit var instance: ServiceLocator
    }
}