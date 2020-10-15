package com.octawizard.server

import com.octawizard.domain.model.Email
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import mu.KotlinLogging

data class AuthorizationException(override val message: String): RuntimeException(message)

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
                        "Authorization failed for ${call.request.path()}. User $tokenUserId can't perform updates on " +
                                "user $userId"
                logger.warn { message }
                throw AuthorizationException(message)
            }
        }
    }


    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Any, UserEmailBasedAuthorization> {
        override val key = AttributeKey<UserEmailBasedAuthorization>("UserEmailBasedAuthorization")

        val AuthorizationPhase = PipelinePhase("Authorization")

        override fun install(pipeline: ApplicationCallPipeline, configure: Any.() -> Unit):
                UserEmailBasedAuthorization {
            return UserEmailBasedAuthorization()
        }


    }
}

class AuthorizedRouteSelector(private val entity: String) : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize by ${entity})"
}

fun Route.authorizeWithUserEmailInPath(pathParameter: String, build: Route.() -> Unit) = authorizedRoute(pathParameter, build)

private fun Route.authorizedRoute(pathParameter: String, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(AuthorizedRouteSelector(pathParameter))
    application.feature(UserEmailBasedAuthorization).interceptPipeline(authorizedRoute, pathParameter)
    authorizedRoute.build()
    return authorizedRoute
}

