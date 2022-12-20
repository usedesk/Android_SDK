package ru.usedesk.chat_sdk.data.repository.form

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.RetrofitApi
import ru.usedesk.chat_sdk.data.repository.api.entity.LoadFields
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository.LoadFormResponse
import ru.usedesk.chat_sdk.data.repository.form.db.DbForm
import ru.usedesk.chat_sdk.data.repository.form.db.FormDao
import ru.usedesk.chat_sdk.data.repository.form.db.FormDatabase
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import javax.inject.Inject

internal class FormRepository @Inject constructor(
    appContext: Context,
    initConfiguration: UsedeskChatConfiguration,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<RetrofitApi>(
    apiFactory,
    multipartConverter,
    gson,
    RetrofitApi::class.java
), IFormRepository {
    private val userKey = initConfiguration.userKey()
    private val formDao: FormDao
    private val mutex = Mutex()
    private val dbGson = Gson()

    init {
        val database = Room.databaseBuilder(
            appContext,
            FormDatabase::class.java,
            "usedesk"
        ).build()
        formDao = database.formDao()
    }

    override suspend fun saveForm(form: UsedeskForm) {
        formDao.save(form.toDb())
    }

    private fun UsedeskForm.toDb(): DbForm {
        val fieldMap = fields.associate {
            it.id to when (it) {
                is Field.CheckBox -> it.checked.toString()
                is Field.List -> it.selected?.id?.toString()
                is Field.Text -> it.text
            }
        }
        val rawFields = dbGson.toJson(fieldMap)
        return DbForm(
            id,
            userKey,
            rawFields,
            state == UsedeskForm.State.SENT
        )
    }

    private suspend fun getDbForm(formId: Long): DbForm? {
        return mutex.withLock { valueOrNull { formDao.get(formId) } }
    }

    override suspend fun loadForm(
        urlChatApi: String,
        clientToken: String,
        form: UsedeskForm
    ): LoadFormResponse {
        val listsId = form.fields
            .asSequence()
            .filterIsInstance<Field.List>()
            .joinToString(",") { it.id.toString() }
        val request = LoadFields.Request(
            clientToken,
            listsId
        )
        val response = doRequestJson(
            urlChatApi,
            request,
            LoadFields.Response::class.java,
            RetrofitApi::loadFieldList
        )
        return when (response?.fields) {
            null -> LoadFormResponse.Error(response?.code)
            else -> {
                val fieldMap = form.fields.associateBy(Field::id)
                val loadedFields = form.fields.mapNotNull {
                    when (it) {
                        is Field.Text -> listOf(it)
                        else -> {
                            val field = response.fields[it.id.toString()]
                            when (field?.get("list")) {
                                null -> field?.convert(it)
                                else -> field.convertToList(fieldMap)
                            }
                        }
                    }
                }.flatten()
                val loadedForm = form.copy(
                    fields = loadedFields,
                    state = UsedeskForm.State.LOADED
                )
                val dbForm = getDbForm(form.id)
                val savedForm = when (dbForm) {
                    null -> loadedForm
                    else -> {
                        val savedFields = dbGson.fromJson(dbForm.fields, JsonObject::class.java)
                        loadedForm.copy(
                            fields = loadedForm.fields.map { field ->
                                when (val savedValue = savedFields[field.id.toString()]?.asString) {
                                    null -> field
                                    else -> when (field) {
                                        is Field.CheckBox -> field.copy(checked = savedValue == "true")
                                        is Field.List -> {
                                            val itemId = savedValue.toLongOrNull()
                                            field.copy(selected = field.items.firstOrNull { it.id == itemId })
                                        }
                                        is Field.Text -> field.copy(text = savedValue)
                                    }
                                }
                            },
                            state = when {
                                dbForm.sent -> UsedeskForm.State.SENT
                                else -> UsedeskForm.State.LOADED
                            }
                        )
                    }
                }
                LoadFormResponse.Done(savedForm)
            }
        }
    }

    class FieldLoadedList(
        val id: Long,
        val children: Array<Children>
    ) {
        class Children(
            val id: Long,
            val value: String,
            val parentFieldId: Long?
        )
    }

    private fun JsonObject.convert(field: Field): List<Field> =
        when (get("ticket_field_type_id")?.asInt) {
            3 -> listOf(
                Field.CheckBox(
                    field.id,
                    field.name,
                    field.required
                )
            )
            1 -> listOf(field)
            else -> listOf()
        }

    private fun JsonObject.convertToList(lists: Map<Long, Field>): List<Field.List>? =
        valueOrNull {
            when (val list = getAsJsonObject("list")) {
                null -> {
                    val fieldLoaded = gson.fromJson(this, FieldLoadedList::class.java)
                    when {
                        fieldLoaded.children.isEmpty() -> null
                        else -> listOfNotNull(
                            (lists[fieldLoaded.id] as? Field.List)?.copy(
                                items = fieldLoaded.children.map {
                                    Field.List.Item(
                                        it.id,
                                        it.value,
                                        it.parentFieldId
                                    )
                                }
                            )
                        )
                    }
                }
                else -> {
                    list.entrySet().mapNotNull {
                        (it.value as JsonObject).convertToList(lists)
                    }.flatten()
                }
            }
        }
}