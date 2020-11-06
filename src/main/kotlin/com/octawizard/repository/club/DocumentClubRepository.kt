package com.octawizard.repository.club

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.reservation.and
import com.octawizard.repository.reservation.filterGeoWithinSphere
import com.octawizard.server.route.entityNotFound
import org.litote.kmongo.exists
import org.litote.kmongo.findOneById
import org.litote.kmongo.keyProjection
import org.litote.kmongo.save
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.updateOneById
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class DocumentClubRepository(private val clubs: MongoCollection<ClubDTO>) : ClubRepository {
    override fun getClub(id: UUID): Club? {
        return clubs.findOneById(id)?.toClub()
    }

    override fun createClub(
        name: String,
        address: String,
        geoLocation: GeoLocation,
        avgPrice: BigDecimal,
        contacts: Contacts,
        fields: List<Field>,
        availability: Availability,
    ): Club {
        val clubDTO = ClubDTO(
            UUID.randomUUID(),
            name,
            address,
            Point(Position(geoLocation.longitude, geoLocation.latitude)),
            fields,
            availability,
            avgPrice,
            contacts,
        )
        clubs.save(clubDTO)
        return clubDTO.toClub()
    }

    private fun updateClubById(clubId: UUID, updateStatement: Any) {
        val result = clubs.updateOneById(clubId, updateStatement)
        if (result.modifiedCount == 1L) {
            entityNotFound<Club>(clubId)
        }
    }

    override fun updateClubName(clubId: UUID, name: String) {
        updateClubById(clubId, ClubDTO::name setTo name)
    }

    override fun updateClubAddress(clubId: UUID, address: String, geoLocation: GeoLocation) {
        val point = Point(Position(geoLocation.longitude, geoLocation.latitude))
        updateClubById(clubId, set(ClubDTO::address setTo address, ClubDTO::geoLocation setTo point))
    }

    override fun updateClubContacts(clubId: UUID, contacts: Contacts) {
        updateClubById(clubId, ClubDTO::contacts setTo contacts)
    }

    override fun updateClubAvgPrice(clubId: UUID, avgPrice: BigDecimal) {
        updateClubById(clubId, ClubDTO::avgPrice setTo avgPrice)
    }

    override fun updateClubFields(clubId: UUID, fields: List<Field>) {
        updateClubById(clubId, ClubDTO::fields setTo fields)
    }

    override fun updateClubAvailability(clubId: UUID, availability: Availability) {
        updateClubById(clubId, ClubDTO::availability setTo availability)
    }

    override fun getNearestClubsAvailableForReservation(
        day: LocalDate,
        latitude: Double,
        longitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club> {
        val filterByAvailabilityDay = Availability::byDate.keyProjection(day).exists() // availability.<day>
        val filterByGeoSphere = filterGeoWithinSphere(
            ClubDTO::geoLocation,
            longitude,
            latitude,
            radius,
            radiusUnit
        )
        return clubs.find(filterByGeoSphere and filterByAvailabilityDay)
            .map { it.toClub() }
            .toList()
    }

    override fun getNearestClubs(
        latitude: Double,
        longitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club> {
        val filterByGeoSphere = filterGeoWithinSphere(
            ClubDTO::geoLocation,
            longitude,
            latitude,
            radius,
            radiusUnit
        )
        return clubs.find(filterByGeoSphere)
            .map { it.toClub() }
            .toList()
    }
}
