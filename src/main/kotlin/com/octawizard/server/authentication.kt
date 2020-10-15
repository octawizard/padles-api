package com.octawizard.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.auth.*
import io.ktor.auth.jwt.*

fun Authentication.Configuration.authenticationConfig() {
    val config = ConfigFactory.load()
    val jwtIssuer = config.getString("jwt.domain")
    val jwtAudience = config.getString("jwt.audience")
    val jwtRealm = config.getString("jwt.realm")
    val algorithm = Algorithm.HMAC256("secret") //todo use secret management
    fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier = JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    jwt {
        realm = jwtRealm
        verifier(makeJwtVerifier(jwtIssuer, jwtAudience))
        validate { credential ->
            val jwtToken =
                    if (credential.payload.audience.contains(jwtAudience))
                        JWTPrincipal(credential.payload)
                    else null
            jwtToken
        }
    }
}
