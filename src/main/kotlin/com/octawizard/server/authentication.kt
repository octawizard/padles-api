package com.octawizard.server

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.Config
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt

const val USER_BASED_AUTH_CONFIG = "user-based"
const val CLUB_BASED_AUTH_CONFIG = "club-based"

fun Authentication.Configuration.authenticationConfig(config: Config) {
    val jwtIssuer = config.getString("jwt.domain")
    val jwtAudience = config.getString("jwt.audience")
    val jwtRealm = config.getString("jwt.realm")
    // todo add expiration

    val userJwtVerifier: (String, String) -> JWTVerifier = setupUserBasedAuthenticationVerifier()
    val clubJwtVerifier: (String, String) -> JWTVerifier = setupClubBasedAuthenticationVerifier()

    jwt(USER_BASED_AUTH_CONFIG) {
        realm = jwtRealm
        verifier {
            userJwtVerifier(jwtIssuer, jwtAudience)
        }
        validate { credential ->
            // todo add expiration check
            val jwtToken =
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else null
            jwtToken
        }
    }

    jwt(CLUB_BASED_AUTH_CONFIG) {
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
    val algorithm = Algorithm.HMAC256("secret") // todo use secret management
    return makeJWTVerifier(algorithm)
}

private fun setupClubBasedAuthenticationVerifier(): (String, String) -> JWTVerifier {
    val algorithm = Algorithm.HMAC256("club-secret") // todo use secret management
    return makeJWTVerifier(algorithm)
}

private fun makeJWTVerifier(algorithm: Algorithm?): (String, String) -> JWTVerifier {
    return fun(issuer: String, audience: String): JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}
