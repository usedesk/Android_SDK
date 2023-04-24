
package ru.usedesk.chat_sdk.data.repository.form

import androidx.core.text.isDigitsOnly
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository._extra.ChatDatabase
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository.LoadFormResponse
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository.SendFormResponse
import ru.usedesk.chat_sdk.data.repository.form.entity.DbForm
import ru.usedesk.chat_sdk.data.repository.form.entity.LoadForm
import ru.usedesk.chat_sdk.data.repository.form.entity.SaveForm
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskForm.Field
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.FieldInfo
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil
import javax.inject.Inject

internal class FormRepository @Inject constructor(
    database: ChatDatabase,
    initConfiguration: UsedeskChatConfiguration,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<FormApi>(
    apiFactory,
    multipartConverter,
    gson,
    FormApi::class.java
), IFormRepository {
    private val userKey = initConfiguration.userKey()
    private val formDao = database.formDao()
    private val mutex = Mutex()
    private val dbGson = Gson()

    override suspend fun saveForm(form: UsedeskForm) {
        formDao.save(form.toDb())
    }

    private fun UsedeskForm.toDb(): DbForm {
        val fieldMap = fields.associate {
            it.id to when (it) {
                is Field.CheckBox -> it.checked.toString()
                is Field.List -> it.selected?.id?.toString()
                is Field.Text -> it.text.trim()
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

    private suspend fun getDbForm(formId: Long) =
        mutex.withLock { valueOrNull { formDao.get(formId) } }

    private fun FieldInfo.toFieldText() = Field.Text(
        id,
        name,
        required,
        type = when (id) {
            "email" -> Field.Text.Type.EMAIL
            "phone" -> Field.Text.Type.PHONE
            "name" -> Field.Text.Type.NAME
            "note" -> Field.Text.Type.NOTE
            "position" -> Field.Text.Type.POSITION
            else -> Field.Text.Type.NONE
        }
    )

    override suspend fun loadForm(
        urlChatApi: String,
        clientToken: String,
        formId: Long,
        fieldsInfo: List<FieldInfo>
    ): LoadFormResponse {
        val ids = fieldsInfo
            .asSequence()
            .filter { it.id.isDigitsOnly() }
            .joinToString(",") { it.id }
        val response = when (ids) {
            "" -> LoadForm.Response(status = 1, fields = mapOf())
            else -> {
                val request = LoadForm.Request(
                    clientToken,
                    ids
                )
                doRequestJson(
                    urlChatApi,
                    request,
                    LoadForm.Response::class.java,
                    FormApi::loadForm
                )
            }
        }
        return when (response?.fields) {
            null -> LoadFormResponse.Error(response?.code)
            else -> {
                val loadedFields = fieldsInfo.flatMap { fieldInfo ->
                    when {
                        fieldInfo.id.isDigitsOnly() -> {
                            val rawField = response.fields[fieldInfo.id]
                            when (val list = rawField?.get("list")) {
                                null, is JsonNull -> listOfNotNull(rawField?.convert(fieldInfo))
                                else -> list.asJsonObject.entrySet().mapNotNull { rawList ->
                                    val listInfo = fieldsInfo.firstOrNull { it.id == rawList.key }
                                    (rawList.value as JsonObject).convertToList(
                                        listInfo?.name,
                                        fieldInfo.required
                                    )
                                }
                            }
                        }
                        else -> listOf(fieldInfo.toFieldText())
                    }
                }
                val loadedForm = UsedeskForm(
                    id = formId,
                    fields = loadedFields,
                    state = UsedeskForm.State.LOADED
                )
                val savedForm = when (val dbForm = getDbForm(formId)) {
                    null -> loadedForm
                    else -> {
                        val savedFields = dbGson.fromJson(dbForm.fields, JsonObject::class.java)
                        loadedForm.copy(
                            fields = loadedForm.fields.map { field ->
                                when (val savedValue = savedFields[field.id]?.asString) {
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

    override fun validateForm(form: UsedeskForm): UsedeskForm {
        val newFields = form.fields.map { field ->
            when (field) {
                is Field.CheckBox -> field.copy(hasError = field.required && !field.checked)
                is Field.List -> {
                    val parentField =
                        form.fields.firstOrNull { it.id == field.parentId } as? Field.List
                    val hasError = (field.required || parentField?.selected != null)
                            && field.selected == null
                    field.copy(hasError = hasError)
                }
                is Field.Text -> {
                    val text = field.text
                    val isValid = when (field.type) {
                        Field.Text.Type.EMAIL -> when {
                            field.required -> UsedeskValidatorUtil.isValidEmailNecessary(text)
                            else -> UsedeskValidatorUtil.isValidEmail(text)
                        }
                        Field.Text.Type.PHONE -> when {
                            field.required -> UsedeskValidatorUtil.isValidPhoneNecessary(text)
                            else -> UsedeskValidatorUtil.isValidPhone(text)
                        }
                        else -> !field.required || text.any(Char::isLetterOrDigit)
                    }
                    field.copy(hasError = !isValid)
                }
            }
        }
        return form.copy(
            fields = newFields.map { field ->
                when (field) {
                    is Field.List -> {
                        val parentField =
                            newFields.firstOrNull { it.id == field.parentId } as? Field.List
                        field.copy(
                            hasError = field.hasError || parentField?.hasError == true
                        )
                    }
                    else -> field
                }
            }
        )
    }

    override suspend fun sendForm(
        urlChatApi: String,
        clientToken: String,
        form: UsedeskForm
    ): SendFormResponse {
        val newFields = form.fields.filter { field ->
            when (field) {
                is Field.List -> field.selected != null
                else -> true
            }
        }.mapNotNull { field ->
            val value = when (field) {
                is Field.CheckBox -> JsonPrimitive(field.checked.toString())
                is Field.List -> {
                    when (field.parentId) {
                        null -> {
                            val lists = form.fields.filterIsInstance<Field.List>()
                            val tree = mutableListOf(field)
                            var lastChild: Field.List? = field
                            while (lastChild != null) {
                                lastChild = lists.firstOrNull { list ->
                                    list.parentId == lastChild?.id
                                }
                                if (lastChild != null) {
                                    tree.add(lastChild)
                                }
                            }
                            when (tree.size) {
                                1 -> when (val selectedId = field.selected?.id?.toString()) {
                                    null -> null
                                    else -> JsonPrimitive(selectedId)
                                }
                                else -> JsonArray().apply {
                                    tree.forEach { list ->
                                        add(JsonObject().apply {
                                            add("id", JsonPrimitive(list.id))
                                            add(
                                                "value",
                                                JsonPrimitive(
                                                    list.selected?.id?.toString() ?: ""
                                                )
                                            )
                                        })
                                    }
                                }
                            }
                        }
                        else -> null
                    }
                }
                is Field.Text -> JsonPrimitive(field.text)
            }
            when (value) {
                null -> null
                else -> SaveForm.Request.Field(
                    field.id,
                    field.required,
                    value
                )
            }
        }
        val request = SaveForm.Request(
            clientToken,
            newFields
        )
        val response = doRequestJson(
            urlChatApi,
            request,
            SaveForm.Response::class.java,
            FormApi::saveForm
        )
        return when (response?.status) {
            1 -> {
                val newForm = form.copy(state = UsedeskForm.State.SENT)
                saveForm(newForm)
                SendFormResponse.Done(newForm)
            }
            else -> SendFormResponse.Error(response?.code)
        }
    }

    private fun JsonObject.getOrNull(key: String) = when (val value = get(key)) {
        is JsonNull -> null
        else -> value
    }

    private fun JsonObject.convert(fieldInfo: FieldInfo): Field? =
        when (getOrNull("ticket_field_type_id")?.asInt) {
            3 -> Field.CheckBox(
                fieldInfo.id,
                fieldInfo.name,
                fieldInfo.required
            )
            2 -> convertToList(fieldInfo.name, fieldInfo.required)
            1 -> Field.Text(
                id = fieldInfo.id,
                name = fieldInfo.name,
                required = fieldInfo.required,
                type = Field.Text.Type.NONE
            )
            else -> null
        }

    private fun JsonObject.convertToList(
        name: String?,
        required: Boolean
    ): Field.List? = valueOrNull {
        val fieldLoaded = gson.fromJson(this, LoadForm.Response.FieldLoadedList::class.java)
        Field.List(
            fieldLoaded.id,
            name ?: fieldLoaded.name,
            required = required,
            items = fieldLoaded.children.map {
                Field.List.Item(
                    it.id,
                    it.value,
                    it.parentOptionId?.toList() ?: listOf()
                )
            },
            parentId = getOrNull("parent_field_id")?.asString
        )
    }
}