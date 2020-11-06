package com.octawizard.repository.club

import com.octawizard.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface ClubRepository {

    fun getClub(id: UUID): Club?

    fun createClub(
        name: String,
        address: String,
        geoLocation: GeoLocation,
        avgPrice: BigDecimal,
        contacts: Contacts,
        fields: List<Field>,
        availability: Availability,
    ): Club

    fun updateClubName(clubId: UUID, name: String) //side effects on reservations
    fun updateClubAddress(clubId: UUID, address: String, geoLocation: GeoLocation) //side effects on reservations
    fun updateClubContacts(clubId: UUID, contacts: Contacts)
    fun updateClubAvgPrice(clubId: UUID, avgPrice: BigDecimal)
    fun updateClubFields(clubId: UUID, fields: List<Field>) //side effects on reservations (?)
    fun updateClubAvailability(clubId: UUID, availability: Availability)

    fun getNearestClubsAvailableForReservation(
        day: LocalDate,
        latitude: Double,
        longitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit
    ) : List<Club>

    fun getNearestClubs(latitude: Double, longitude: Double, radius: Double, radiusUnit: RadiusUnit): List<Club>

}
