package ru.usedesk.chat_sdk.data.repository.form

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.form.entity.DbForm
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskForm
import javax.inject.Inject

internal class FormRepository @Inject constructor(
    private val appContext: Context,
    private val initConfiguration: UsedeskChatConfiguration
) : IFormRepository {
    private val userKey = initConfiguration.userKey()
    private val formDao: FormDao
    private val databaseName = "usedesk_form_${userKey}"
    private val mutex = Mutex()

    init {
        val database = Room.databaseBuilder(
            appContext,
            FormDatabase::class.java,
            databaseName
        ).build()
        formDao = database.formDao()
    }

    override suspend fun saveForm(form: UsedeskForm) {
        formDao.save(form.toDb())
    }

    private fun UsedeskForm.toDb() = DbForm(
        id,
        state == UsedeskForm.State.SENT
    )

    override suspend fun loadForm(form: UsedeskForm) = mutex.withLock {
        val rawQuery = SimpleSQLiteQuery(
            "SELECT * FROM $databaseName WHERE id = ${form.id} LIMIT 1"
        )
        when (val dbForm = formDao.get(rawQuery)) {
            null -> form
            else -> form.copy(
                fields = form.fields.map {
                    it //TODO
                },
                state = when {
                    dbForm.sent -> UsedeskForm.State.SENT
                    else -> UsedeskForm.State.LOADED
                }
            )
        }
    }
}