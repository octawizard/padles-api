package com.octawizard.server.route

import io.ktor.application.*
import io.ktor.features.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

inline fun <reified T> entityNotFound(id: Any): T {
    throw NotFoundException("${T::class.simpleName} $id not found")
}

fun ApplicationCall.getDoubleQueryParam(name: String): Double? {
    return request.queryParameters[name]?.toDoubleOrNull()
}

inline fun <reified T : Enum<T>> ApplicationCall.getEnumQueryParamOrDefault(name: String, default: T): T {
    return request.queryParameters[name]?.let { enumValueOf<T>(it) } ?: default
}

fun ApplicationCall.getLocalDateQueryParam(name: String, dateTimeFormatter: DateTimeFormatter): LocalDate? {
    return request.queryParameters[name]?.toLocalDateOrNull(dateTimeFormatter)
}

private fun String.toLocalDateOrNull(dateTimeFormatter: DateTimeFormatter): LocalDate? {
    return try {
        LocalDate.parse(this, dateTimeFormatter)
    } catch (e: DateTimeParseException) {
        return null
    }
}
