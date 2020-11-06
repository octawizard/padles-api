package com.octawizard.server.route

import com.octawizard.controller.Controller
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.server.input.CreateClubInput
import com.octawizard.server.input.UpdateClubAddressNameInput
import com.octawizard.server.input.UpdateClubAvgPriceInput
import com.octawizard.server.input.UpdateClubContactsInput
import com.octawizard.server.input.UpdateClubNameInput
import com.octawizard.server.route.QueryParams.DAY
import com.octawizard.server.route.QueryParams.LATITUDE
import com.octawizard.server.route.QueryParams.LONGITUDE
import com.octawizard.server.route.QueryParams.RADIUS
import com.octawizard.server.route.QueryParams.RADIUS_UNIT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.time.format.DateTimeFormatter
import java.util.*

@Location("/club/clubId")
data class ClubIdRoute(val clubId: UUID)

@Location("/club/clubId/name")
data class ClubNameRoute(val clubId: UUID)

@Location("/club/clubId/address")
data class ClubAddressRoute(val clubId: UUID)

@Location("/club/clubId/contacts")
data class ClubContactsRoute(val clubId: UUID)

@Location("/club/clubId/avgPrice")
data class ClubAvgPriceRoute(val clubId: UUID)

@Location("/club/clubId/fields")
data class ClubFieldsRoute(val clubId: UUID)

@Location("/club/clubId/field/fieldId")
data class ClubFieldRoute(val clubId: UUID, val fieldId: UUID)

@Location("/club/clubId/availability")
data class ClubAvailabilityRoute(val clubId: UUID)

object QueryParams {
    const val LONGITUDE = "lon"
    const val LATITUDE = "lat"
    const val RADIUS = "rad"
    const val RADIUS_UNIT = "radUnit"
    const val DAY = "day"
}

fun Routing.clubRoutes(controller: Controller) {
    authenticate {

        // get club
        get<ClubIdRoute> { route ->
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

        // search near clubs
        get("/clubs") {
            val longitude = call.getDoubleQueryParam(LONGITUDE)
            val latitude = call.getDoubleQueryParam(LATITUDE)
            val radius = call.getDoubleQueryParam(RADIUS)
            val radiusUnit = call.getEnumQueryParamOrDefault(RADIUS_UNIT, RadiusUnit.Kilometers)

            checkNotNull(longitude)
            checkNotNull(latitude)
            checkNotNull(radius)

            val clubs = controller.getNearestClubs(longitude, latitude, radius, radiusUnit)
            call.respond(HttpStatusCode.OK, clubs)
        }

        // search near clubs with availability for a specific day
        get("/clubs") {
            val longitude = call.getDoubleQueryParam(LONGITUDE)
            val latitude = call.getDoubleQueryParam(LATITUDE)
            val radius = call.getDoubleQueryParam(RADIUS)
            val radiusUnit = call.getEnumQueryParamOrDefault(RADIUS_UNIT, RadiusUnit.Kilometers)
            val day = call.getLocalDateQueryParam(DAY, DateTimeFormatter.ISO_LOCAL_DATE)

            checkNotNull(longitude)
            checkNotNull(latitude)
            checkNotNull(radius)
            checkNotNull(day)

            val clubs = controller.getAvailableNearestClubs(longitude, latitude, radius, radiusUnit, day)
            call.respond(HttpStatusCode.OK, clubs)
        }

        // all updates for club
        put<ClubNameRoute> { route ->
            val input = call.receive<UpdateClubNameInput>()
            //todo check if authorized   payload.sub == club.contacts.email
            val club = controller.getClub(route.clubId) ?: entityNotFound(route.clubId)

            val updatedClub = controller.updateClubName(club, input.name)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubAddressRoute> { route ->
            val input = call.receive<UpdateClubAddressNameInput>()
            //todo check if authorized   payload.sub == club.contacts.email
            val club = controller.getClub(route.clubId) ?: entityNotFound(route.clubId)
            val updatedClub = controller.updateClubAddress(club, input.address, input.location)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

        put<ClubContactsRoute> { route ->
            val input = call.receive<UpdateClubContactsInput>()
            //todo check if authorized
            val club = controller.getClub(route.clubId) ?: entityNotFound(route.clubId)
            val contacts = Contacts(input.phone, input.email)
            val updatedClub = controller.updateClubContacts(club, contacts)
            call.respond(HttpStatusCode.OK, updatedClub)
        }

//        put<ClubAvgPriceRoute> { route ->
//            val input = call.receive<UpdateClubAvgPriceInput>()
//            //todo check if authorized
//            val club = controller.updateClubAvgPrice(route.clubId, input.avgPrice)
//            call.respond(HttpStatusCode.OK, club)
//        }
//        //fields
//        put<ClubFieldsRoute> { route ->
//            val input = call.receive<UpdateClubFieldsInput>()
//            //todo check if authorized
//            val club = controller.updateClubFields(route.clubId, input.fields)
//            call.respond(HttpStatusCode.OK, club)
//        }
//
//        put<ClubFieldRoute> { route ->
//            val input = call.receive<UpdateClubFieldInput>()
//            //todo check if authorized
//            val club = controller.updateClubField(route.clubId, route.fieldId, input.field)
//            call.respond(HttpStatusCode.OK, club)
//        }
//
//        //availability
//        put<ClubAvailabilityRoute> { route ->
//            val input = call.receive<UpdateClubAvailabilityInput>()
//            //todo check if authorized
//            val club = controller.updateClubAvailability(route.clubId, input.availability)
//            call.respond(HttpStatusCode.OK, club)
//        }
    }
}
