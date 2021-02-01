package com.octawizard.server.route

import com.octawizard.controller.club.ClubController
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.server.AuthorizationException
import com.octawizard.server.input.AddClubFieldInput
import com.octawizard.server.input.ClubSearchCriteria
import com.octawizard.server.input.CreateClubInput
import com.octawizard.server.input.UpdateClubAddressInput
import com.octawizard.server.input.UpdateClubAvailabilityInput
import com.octawizard.server.input.UpdateClubAvgPriceInput
import com.octawizard.server.input.UpdateClubContactsInput
import com.octawizard.server.input.UpdateClubFieldInput
import com.octawizard.server.input.UpdateClubNameInput
import com.octawizard.server.route.QueryParams.CRITERIA
import com.octawizard.server.route.QueryParams.DAY
import com.octawizard.server.route.QueryParams.LATITUDE
import com.octawizard.server.route.QueryParams.LONGITUDE
import com.octawizard.server.route.QueryParams.NAME
import com.octawizard.server.route.QueryParams.RADIUS
import com.octawizard.server.route.QueryParams.RADIUS_UNIT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import java.time.format.DateTimeFormatter
import java.util.*

@Location("/club/{clubIdString}")
data class ClubRoute(private val clubIdString: String) {
    val clubId: UUID = UUID.fromString(clubIdString)

    @Location("/name")
    data class Name(val parent: ClubRoute)

    @Location("/address")
    data class Address(val parent: ClubRoute)

    @Location("/contacts")
    data class Contacts(val parent: ClubRoute)

    @Location("/avgPrice")
    data class AvgPrice(val parent: ClubRoute)

    @Location("/fields")
    data class Fields(val parent: ClubRoute)

    @Location("/field/{fieldId}")
    data class Field(val parent: ClubRoute, val fieldId: UUID)

    @Location("/availability")
    data class Availability(val parent: ClubRoute)
}

object QueryParams {
    const val CRITERIA = "criteria"
    const val NAME = "name"
    const val LONGITUDE = "lon"
    const val LATITUDE = "lat"
    const val RADIUS = "rad"
    const val RADIUS_UNIT = "radUnit"
    const val DAY = "day"
}

fun Routing.clubRoutes(controller: ClubController) {
    authenticate("club-based") {

        // get club
        get<ClubRoute> { route ->
            val club = controller.getClub(route.clubId) ?: entityNotFound(route.clubId)
            call.respond(HttpStatusCode.OK, club)
        }

        // create club
        post("/club") {
            val input = call.receive<CreateClubInput>()
            val club = controller.createClub(
                input.name,
                input.address,
                input.geoLocation,
                input.avgPrice,
                input.contacts,
                input.fields,
                input.availability,
            )
            call.respond(HttpStatusCode.Created, club)
        }

        // search clubs by name, distance or distance + day availability
        get("/clubs") { //clubs?criteria=ByName&name=padel-club
            when (call.getEnumQueryParamOrDefault(CRITERIA, ClubSearchCriteria.ByName)) {
                ClubSearchCriteria.ByName -> searchClubsByName(controller)
                ClubSearchCriteria.ByDistance -> searchClubsByDistance(controller)
                ClubSearchCriteria.ByDistanceAndDayAvailability -> searchClubsByDistanceAndDayAvailability(controller)
            }
        }

        // all updates for club
        put<ClubRoute.Name> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubNameInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)

            val updatedClub = controller.updateClubName(club, input.name)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubRoute.Address> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubAddressInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)
            val updatedClub = controller.updateClubAddress(club, input.address, input.location)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubRoute.Contacts> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubContactsInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)
            val contacts = Contacts(input.phone, input.email)
            val updatedClub = controller.updateClubContacts(club, contacts)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubRoute.AvgPrice> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubAvgPriceInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)
            val updatedClub = controller.updateClubAvgPrice(club, input.avgPrice)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        //fields (only add new field)
        post<ClubRoute.Fields> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<AddClubFieldInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)
            val updatedClub = controller.addToClubFields(
                club, input.name, input.isIndoor, input.hasSand, input.wallsMaterial
            )
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubRoute.Field> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubFieldInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)

            val updatedClub = controller.updateClubField(
                club,
                route.fieldId,
                input.name,
                input.isIndoor,
                input.hasSand,
                input.wallsMaterial
            )
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        //availability
        put<ClubRoute.Availability> { route ->
            authorize(route.parent.clubId)
            val input = call.receive<UpdateClubAvailabilityInput>()
            val club = controller.getClub(route.parent.clubId) ?: entityNotFound(route.parent.clubId)
            val updatedClub = controller.updateClubAvailability(club, input.availability)
            call.respond(HttpStatusCode.OK, updatedClub)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.authorize(clubId: UUID) {
    val principal = call.authentication.principal<JWTPrincipal>() ?: throw AuthorizationException("Missing principal")
    val tokenClubId = principal.payload.subject
    if (clubId.toString() != tokenClubId) {
        throw AuthorizationException("Not authorized to update/delete club - not owned by $tokenClubId")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchClubsByDistanceAndDayAvailability(
    controller: ClubController,
) {
    val longitude = call.getDoubleQueryParam(LONGITUDE)
    val latitude = call.getDoubleQueryParam(LATITUDE)
    val radius = call.getDoubleQueryParam(RADIUS)
    val radiusUnit = call.getEnumQueryParamOrDefault(RADIUS_UNIT, RadiusUnit.Kilometers)
    val day = call.getLocalDateQueryParam(DAY, DateTimeFormatter.ISO_LOCAL_DATE)

    checkNotNull(longitude) { "query param longitude cannot be null" }
    checkNotNull(latitude) { "query param latitude cannot be null" }
    checkNotNull(radius) { "query param radius cannot be null" }
    checkNotNull(day) { "query param day cannot be null" }

    val clubs = controller.getAvailableNearestClubs(longitude, latitude, radius, radiusUnit, day)
    call.respond(HttpStatusCode.OK, clubs)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchClubsByDistance(
    controller: ClubController,
) {
    val longitude = call.getDoubleQueryParam(LONGITUDE)
    val latitude = call.getDoubleQueryParam(LATITUDE)
    val radius = call.getDoubleQueryParam(RADIUS)
    val radiusUnit = call.getEnumQueryParamOrDefault(RADIUS_UNIT, RadiusUnit.Kilometers)

    checkNotNull(longitude) { "query param longitude cannot be null" }
    checkNotNull(latitude) { "query param latitude cannot be null" }
    checkNotNull(radius) { "query param radius cannot be null" }

    val clubs = controller.getNearestClubs(longitude, latitude, radius, radiusUnit)
    call.respond(HttpStatusCode.OK, clubs)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchClubsByName(
    controller: ClubController,
) {
    val name = call.request.queryParameters[NAME]
    check(!name.isNullOrEmpty()) { "query param name cannot be null or empty" }

    val clubs = controller.searchClubsByName(name)
    call.respond(HttpStatusCode.OK, clubs)
}
