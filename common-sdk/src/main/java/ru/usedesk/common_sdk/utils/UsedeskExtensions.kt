package ru.usedesk.common_sdk.utils

import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson

private val gson = Gson()

private fun <T> toJson(obj: T): String = gson.toJson(obj)

private fun <T> fromJson(json: String?, clazz: Class<T>): T? {
    return if (json != null) {
        try {
            Gson().fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

fun <T> Bundle.putAsJson(name: String, obj: T) {
    val json = toJson(obj)
    putString(name, json)
}

fun <T> Bundle.getFromJson(name: String, clazz: Class<T>): T? {
    val json = getString(name)
    return fromJson(json, clazz)
}

fun <T> Intent.putAsJsonExtra(name: String, obj: T) {
    val json = toJson(obj)
    putExtra(name, json)
}

fun <T> Intent.getFromJsonExtra(name: String, clazz: Class<T>): T? {
    val json = getStringExtra(name)
    return fromJson(json, clazz)
}