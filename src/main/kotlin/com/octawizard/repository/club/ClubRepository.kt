package com.octawizard.repository.club

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.WallsMaterial
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
        fields: Set<Field>,
        availability: Availability,
    ): Club

    fun updateClubName(clubId: UUID, name: String) // side effects on reservations
    fun updateClubAddress(clubId: UUID, address: String, geoLocation: GeoLocation) // side effects on reservations
    fun updateClubContacts(clubId: UUID, contacts: Contacts)
    fun updateClubAvgPrice(clubId: UUID, avgPrice: BigDecimal)
    fun updateClubField(
        clubId: UUID,
        fieldId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    )

    fun addFieldToClub(
        clubId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Field

    fun updateClubAvailability(clubId: UUID, availability: Availability)
    fun addClubAvailability(clubId: UUID, fieldAvailability: FieldAvailability)

    fun getNearestClubsAvailableForReservation(
        day: LocalDate,
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club>

    fun getNearestClubs(longitude: Double, latitude: Double, radius: Double, radiusUnit: RadiusUnit): List<Club>

    fun searchClubsByName(name: String): List<Club>
}
