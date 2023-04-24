
package ru.usedesk.chat_sdk.data.repository.form.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal class DbForm(
    @PrimaryKey
    val id: Long,
    val userKey: String,
    val fields: String,
    val sent: Boolean
)