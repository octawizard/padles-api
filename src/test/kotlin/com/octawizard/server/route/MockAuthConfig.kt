package com.octawizard.server.route

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.octawizard.server.ClubBasedAuthenticationConfig
import com.octawizard.server.UserBasedAuthenticationConfig
import com.octawizard.server.route.MockAuthConfig.algorithm
import com.octawizard.server.route.MockAuthConfig.jwtAudience
import com.octawizard.server.route.MockAuthConfig.jwtIssuer
import com.octawizard.server.route.MockAuthConfig.jwtRealm
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import java.time.Instant
import java.util.*

object MockAuthConfig {

    val jwtIssuer = "padles.issuer"
    val jwtAudience = "padles.audience"
    val jwtRealm = "padles.realm"
    val secret = "dummy.secret"
    val algorithm = Algorithm.HMAC256(secret)

    /**
     * Produce a token for this combination of name and password
     */
    fun generateToken(id: String): String = JWT.create()
        .withSubject(id)
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .withClaim("realm", jwtRealm)
        .withIssuedAt(Date.from(Instant.now()))
        .sign(algorithm)
}

fun Authentication.Configuration.mockAuthConfig() {

    fun makeJWTVerifier(algorithm: Algorithm?): (String, String) -> JWTVerifier {
        return fun(issuer: String, audience: String): JWTVerifier = JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }

    val userJwtVerifier: (String, String) -> JWTVerifier = makeJWTVerifier(algorithm)
    val clubJwtVerifier: (String, String) -> JWTVerifier = makeJWTVerifier(algorithm)

    jwt(UserBasedAuthenticationConfig) {
        realm = jwtRealm
        verifier {
            userJwtVerifier(jwtIssuer, jwtAudience)
        }
        validate { credential ->
            val jwtToken =
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else null
            jwtToken
        }
    }

    jwt(ClubBasedAuthenticationConfig) {
        realm = jwtRealm
        verifier {
            clubJwtVerifier(jwtIssuer, jwtAudience)
        }
        validate { credential ->
            val jwtToken =
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else null
            jwtToken
        }
    }
}
