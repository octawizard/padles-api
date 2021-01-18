package com.octawizard.server.route

import io.ktor.http.*
import io.ktor.server.testing.*

fun TestApplicationEngine.handleRequestWithJWT(
    method: HttpMethod,
    url: String,
    id: String,
): TestApplicationCall =
    handleRequest {
        this.uri = url
        this.method = method
        addHeader(HttpHeaders.Authorization, "Bearer ${MockAuthConfig.generateToken(id)}")
    }
