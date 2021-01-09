package com.octawizard.repository.club

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.TextSearchOptions
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.model.AvailabilityDTO
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.club.model.DateFormatter
import com.octawizard.repository.club.model.toAvailabilityDTO
import com.octawizard.repository.reservation.and
import com.octawizard.repository.reservation.filterGeoWithinSphere
import com.octawizard.server.route.entityNotFound
import org.bson.conversions.Bson
import org.litote.kmongo.SetTo
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.colProperty
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.exists
import org.litote.kmongo.findOneById
import org.litote.kmongo.keyProjection
import org.litote.kmongo.ne
import org.litote.kmongo.save
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.text
import org.litote.kmongo.updateOne
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
        fields: Set<Field>,
        availability: Availability,
    ): Club {
        val clubDTO = ClubDTO(
            UUID.randomUUID(),
            name,
            address,
            Point(Position(geoLocation.longitude, geoLocation.latitude)),
            fields,
            availability.toAvailabilityDTO(),
            avgPrice,
            contacts,
        )
        clubs.save(clubDTO)
        return clubDTO.toClub()
    }

    private fun updateClubById(clubId: UUID, updateStatement: Any) {
        val idFilter = ClubDTO::id eq clubId
        val result = when(updateStatement){
            is SetTo<*> -> clubs.updateOne(idFilter, updateStatement)
            is Bson -> clubs.updateOne(idFilter, updateStatement)
            else -> throw IllegalArgumentException("unrecognized update statement")
        }

        if (result.modifiedCount != 1L) {
            entityNotFound<Club>(clubId)
        }
    }

    override fun updateClubName(clubId: UUID, name: String) {
        updateClubById(clubId, ClubDTO::name setTo name)
    }

    override fun updateClubAddress(clubId: UUID, address: String, geoLocation: GeoLocation) {
        val point = Point(Position(geoLocation.longitude, geoLocation.latitude))
        val updateStatement = set(ClubDTO::address setTo address, ClubDTO::geoLocation setTo point)
        updateClubById(clubId, updateStatement)
    }

    override fun updateClubContacts(clubId: UUID, contacts: Contacts) {
        updateClubById(clubId, ClubDTO::contacts setTo contacts)
    }

    override fun updateClubAvgPrice(clubId: UUID, avgPrice: BigDecimal) {
        updateClubById(clubId, ClubDTO::avgPrice setTo avgPrice)
    }

    override fun updateClubField(
        clubId: UUID,
        fieldId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ) {
        val result = clubs.updateOne(
            (ClubDTO::id eq clubId) and ((ClubDTO::fields / Field::id) eq fieldId),
            ClubDTO::fields.colProperty.posOp / Field::name setTo name,
            ClubDTO::fields.colProperty.posOp / Field::isIndoor setTo indoor,
            ClubDTO::fields.colProperty.posOp / Field::wallsMaterial setTo wallsMaterial,
            ClubDTO::fields.colProperty.posOp / Field::hasSand setTo hasSand,
        )
        if (result.modifiedCount != 1L) {
            entityNotFound<Club>(clubId)
        }
    }

    override fun addFieldToClub(
        clubId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Field {
        val field = Field(UUID.randomUUID(), name, indoor, wallsMaterial, hasSand)
        updateClubById(clubId, addToSet(ClubDTO::fields, field))
        return field
    }

    override fun updateClubAvailability(clubId: UUID, availability: Availability) {
        updateClubById(clubId, ClubDTO::availability setTo availability.toAvailabilityDTO())
    }

    override fun addClubAvailability(clubId: UUID, fieldAvailability: FieldAvailability) {
        val date = fieldAvailability.timeSlot.startDateTime.toLocalDate().format(DateFormatter)
        updateClubById(
            clubId, addToSet((ClubDTO::availability / AvailabilityDTO::byDate).keyProjection(date), fieldAvailability)
        )
    }

    override fun getNearestClubsAvailableForReservation(
        day: LocalDate,
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club> {
        val dayString = day.format(DateFormatter)
        val availabilityByDayProperty = (ClubDTO::availability / AvailabilityDTO::byDate).keyProjection(dayString)
        val filterByAvailabilityDay = and(
            availabilityByDayProperty.exists(),
            availabilityByDayProperty.ne(emptyList())
        )
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
        longitude: Double,
        latitude: Double,
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

    override fun searchClubsByName(name: String): List<Club> {
        return clubs.find(text(name, TextSearchOptions().caseSensitive(false)))
            .map { it.toClub() }
            .toList()
    }
}
