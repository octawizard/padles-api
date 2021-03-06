package com.octawizard.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.auth.*
import io.ktor.auth.jwt.*

const val UserBasedAuthenticationConfig = "user-based"
const val ClubBasedAuthenticationConfig = "club-based"

fun Authentication.Configuration.authenticationConfig() {
    val config = ConfigFactory.load()
    val jwtIssuer = config.getString("jwt.domain")
    val jwtAudience = config.getString("jwt.audience")
    val jwtRealm = config.getString("jwt.realm")
    //todo add expiration

    val userJwtVerifier: (String, String) -> JWTVerifier = setupUserBasedAuthenticationVerifier()
    val clubJwtVerifier: (String, String) -> JWTVerifier = setupClubBasedAuthenticationVerifier()

    jwt(UserBasedAuthenticationConfig) {
        realm = jwtRealm
        verifier {
            userJwtVerifier(jwtIssuer, jwtAudience)
        }
        validate { credential ->
            //todo add expiration check
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

private fun setupUserBasedAuthenticationVerifier(): (String, String) -> JWTVerifier {
    val algorithm = Algorithm.HMAC256("secret") //todo use secret management
    return makeJWTVerifier(algorithm)
}

private fun setupClubBasedAuthenticationVerifier(): (String, String) -> JWTVerifier {
    val algorithm = Algorithm.HMAC256("club-secret") //todo use secret management
    return makeJWTVerifier(algorithm)
}

private fun makeJWTVerifier(algorithm: Algorithm?): (String, String) -> JWTVerifier {
    return fun(issuer: String, audience: String): JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}
