package com.octawizard.server

import com.octawizard.domain.model.Email
import com.octawizard.server.route.USER_EMAIL_PARAM
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.auth.Authentication
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.features.BadRequestException
import io.ktor.request.path
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import mu.KotlinLogging

data class AuthorizationException(override val message: String) : RuntimeException(message)

class UserEmailBasedAuthorization {
    private val logger = KotlinLogging.logger { }

    fun interceptPipeline(pipeline: ApplicationCallPipeline, pathParam: String) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        pipeline.insertPhaseAfter(Authentication.ChallengePhase, AuthorizationPhase)

        pipeline.intercept(AuthorizationPhase) {
            val principal =
                call.authentication.principal<JWTPrincipal>() ?: throw AuthorizationException("Missing principal")

            val tokenUserId = Email(principal.payload.subject)
            val userIdFromPath = (call.parameters[pathParam] ?: throw BadRequestException("user id must be in path"))
            val userId = Email(userIdFromPath)

            if (userId != tokenUserId) {
                val message =
                    "Authorization failed for ${call.request.path()}. User $tokenUserId can't perform updates on user $userId"
                logger.warn { message }
                throw AuthorizationException(message)
            }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Any, UserEmailBasedAuthorization> {
        override val key = AttributeKey<UserEmailBasedAuthorization>("UserEmailBasedAuthorization")

        val AuthorizationPhase = PipelinePhase("Authorization")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Any.() -> Unit
        ): UserEmailBasedAuthorization {
            return UserEmailBasedAuthorization()
        }
    }
}

class AuthorizedRouteSelector(private val entity: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize by $entity)"
}

fun Route.authorizeWithUserEmailInPath(build: Route.() -> Unit) = authorizedRoute(USER_EMAIL_PARAM, build)

private fun Route.authorizedRoute(pathParameter: String, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(AuthorizedRouteSelector(pathParameter))
    application.feature(UserEmailBasedAuthorization).interceptPipeline(authorizedRoute, pathParameter)
    authorizedRoute.build()
    return authorizedRoute
}
