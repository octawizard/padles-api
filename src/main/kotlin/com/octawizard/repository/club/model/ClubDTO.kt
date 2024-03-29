package com.octawizard.repository.club.model

import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import java.math.BigDecimal
import java.util.*

data class ClubDTO(
    val id: UUID,
    val name: String,
    val address: String,
    val geoLocation: Point,
    val fields: Set<Field>,
    val availability: AvailabilityDTO,
    val avgPrice: BigDecimal,
    val contacts: Contacts,
) {

    fun toClub(): Club {
        return Club(
            id,
            name,
            address,
            GeoLocation(geoLocation.position.values[0], geoLocation.position.values[1]),
            fields,
            availability.toAvailability(),
            avgPrice,
            contacts,
        )
    }
}

fun Club.toClubDTO(): ClubDTO =
    ClubDTO(
        id,
        name,
        address,
        Point(Position(geoLocation.longitude, geoLocation.latitude)),
        fields,
        availability.toAvailabilityDTO(),
        avgPrice,
        contacts,
    )
