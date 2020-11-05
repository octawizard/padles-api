package com.octawizard.repository.reservation

import com.mongodb.client.model.Filters
import com.octawizard.domain.model.MATCH_MAX_NUMBER_OF_PLAYERS
import com.octawizard.domain.model.RadiusUnit
import org.bson.conversions.Bson

infix fun Bson.and(filter: Bson?): Bson = Filters.and(this, filter)

fun filterReservationsWithinSphere(longitude: Double, latitude: Double, radius: Double, radiusUnit: RadiusUnit): Bson =
    Filters.geoWithinCenterSphere(
        "clubReservationInfo.location",
        longitude,
        latitude,
        radius / radiusUnit.earthRadius
    )

fun filterReservationWithMissingPlayers(): Bson = Filters.lt("match.playersCount", MATCH_MAX_NUMBER_OF_PLAYERS)
