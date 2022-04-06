package com.octawizard.server.route

import com.octawizard.server.serialization.contextualSerializers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.decodeURLQueryComponent
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.setBody
import kotlinx.serialization.json.Json

val JsonSerde = Json {
    serializersModule = contextualSerializers
    allowStructuredMapKeys = true
}

fun TestApplicationEngine.handleRequestWithJWT(
    method: HttpMethod,
    url: String,
    id: String,
    queryParams: Map<String, String> = emptyMap(),
    body: String? = null,
): TestApplicationCall =
    handleRequest {
        this.uri = url + queryParams.toQueryParamString()
        this.method = method
        addHeader(HttpHeaders.Authorization, "Bearer ${MockAuthConfig.generateToken(id)}")
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        body?.let { this.setBody(body) }
    }

fun Map<String, String>.toQueryParamString(): String {
    if (this.isEmpty()) {
        return ""
    }
    val queryParams = this.map { e -> "${e.key}=${e.value}".decodeURLQueryComponent() }.joinToString("&")
    return "?$queryParams"
}
