package com.octawizard.server.route

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.testing.*

val gson = Gson()

fun TestApplicationEngine.handleRequestWithJWT(
    method: HttpMethod,
    url: String,
    id: String,
    body: Any? = null,
    queryParams: Map<String, String> = emptyMap()
): TestApplicationCall =
    handleRequest {
        this.uri = url + queryParams.toQueryParamString()
        this.method = method
        addHeader(HttpHeaders.Authorization, "Bearer ${MockAuthConfig.generateToken(id)}")
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        body?.let { this.setBody(gson.toJson(it)) }
    }


fun Map<String, String>.toQueryParamString(): String {
    if (this.isEmpty()) {
        return ""
    }
    val queryParams = this.map { e -> "${e.key}=${e.value}".decodeURLQueryComponent() }.joinToString("&")
    return "?$queryParams"
}
