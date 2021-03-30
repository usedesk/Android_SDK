package ru.usedesk.sample

import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository

object ServiceLocator {
    lateinit var configurationRepository: ConfigurationRepository

    fun init(appContext: Context) {
        val workScheduler: Scheduler = Schedulers.io()
        val sharedPreferences = appContext.getSharedPreferences("SampleConfiguration", Context.MODE_PRIVATE)
        configurationRepository = ConfigurationRepository(sharedPreferences, workScheduler)
    }
}