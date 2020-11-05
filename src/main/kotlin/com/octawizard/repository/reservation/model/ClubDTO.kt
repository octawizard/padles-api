package com.octawizard.repository.reservation.model

import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import java.util.*

data class ClubReservationInfoDTO(
    val id: UUID,
    val name: String,
    val field: Field,
    val location: Point
) {
    fun toClubReservationInfo(): ClubReservationInfo {
        return ClubReservationInfo(
            id, name, field, GeoLocation(
                location.coordinates.values[0], location.coordinates
                    .values[1]
            )
        )
    }
}

fun ClubReservationInfo.toClubReservationInfoDTO(): ClubReservationInfoDTO {
    return ClubReservationInfoDTO(
        id = this.clubId,
        name = this.name,
        field = this.field,
        location = Point(Position(this.clubLocation.longitude, this.clubLocation.latitude))
    )
}
