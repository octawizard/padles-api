package com.octawizard.server.route

import io.ktor.application.ApplicationCall
import io.ktor.features.NotFoundException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

inline fun <reified T> entityNotFound(id: Any): T {
    throw NotFoundException("${T::class.simpleName} $id not found")
}

inline fun <reified T : Enum<T>> ApplicationCall.getEnumQueryParamOrDefault(name: String, default: T): T {
    return request.queryParameters[name]?.let { enumValueOf<T>(it) } ?: default
}

inline fun <reified T> ApplicationCall.getQueryParamOrDefault(
    name: String,
    default: T? = null,
    dateTimeFormatter: DateTimeFormatter? = null,
): T? {
    return when (T::class.qualifiedName) {
        Int::class.qualifiedName -> request.queryParameters[name]?.toInt() as T? ?: default
        Double::class.qualifiedName -> request.queryParameters[name]?.toDouble() as T? ?: default
        Long::class.qualifiedName -> request.queryParameters[name]?.toLong() as T? ?: default
        String::class.qualifiedName -> request.queryParameters[name] as T? ?: default
        LocalDate::class.qualifiedName -> {
            checkNotNull(dateTimeFormatter)
            return request.queryParameters[name]?.toLocalDateOrNull(dateTimeFormatter) as T? ?: default
        }
        else -> request.queryParameters[name] as T? ?: default
    }
}

fun String.toLocalDateOrNull(dateTimeFormatter: DateTimeFormatter): LocalDate? {
    return try {
        LocalDate.parse(this, dateTimeFormatter)
    } catch (e: DateTimeParseException) {
        return null
    }
}
