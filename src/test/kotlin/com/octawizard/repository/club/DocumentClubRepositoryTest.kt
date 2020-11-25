package com.octawizard.repository.club

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.MongoBaseTestWithUUIDRepr
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.club.model.toClubDTO
import io.ktor.features.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.save
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentClubRepositoryTest : MongoBaseTestWithUUIDRepr<ClubDTO>() {
    private val repository = DocumentClubRepository(col)
    private val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)

    @BeforeEach
    fun beforeEach() {
        col.drop()
    }

    private fun getClub(id: UUID): Club =
        Club(
            id,
            "club name",
            "club address",
            GeoLocation(1.1, 1.1),
            setOf(field),
            EmptyAvailability,
            BigDecimal.TEN,
            Contacts("21451", Email("club@test.com")),
        )

    @Test
    fun `DocumentClubRepository should return a club given its id if it exists`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        assertEquals(club, repository.getClub(clubId))
    }

    @Test
    fun `DocumentClubRepository should return null when getting a club if club doesn't exists`() {
        val clubId = UUID.randomUUID()

        assertNull(repository.getClub(clubId))
    }

    @Test
    fun `DocumentClubRepository should create a club and returns it`() {
        val now = LocalDateTime.now()
        val fieldAvailability = FieldAvailability(TimeSlot(now.plusHours(1), now.plusHours(2)), field, BigDecimal.TEN)
        val club = getClub(UUID.randomUUID())
            .copy(availability = Availability(mapOf(now.toLocalDate() to listOf(fieldAvailability))))

        val createdClub = repository.createClub(
            club.name,
            club.address,
            club.geoLocation,
            club.avgPrice,
            club.contacts,
            club.fields,
            club.availability.copy(byDate = mapOf(now.toLocalDate() to listOf(fieldAvailability))),
        )
        assertEquals(club.copy(id = createdClub.id), createdClub)
    }

    @Test
    fun `DocumentClubRepository should update a club name`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val updatedName = "update name"
        repository.updateClubName(club.id, updatedName)

        assertEquals(updatedName, repository.getClub(club.id)?.name)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating a club name for a non existing club`() {
        assertThrows(NotFoundException::class.java) { repository.updateClubName(UUID.randomUUID(), "update name") }
    }

    @Test
    fun `DocumentClubRepository should update a club address`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val address = "update address"
        val geoLocation = GeoLocation(10.1, 10.1)
        repository.updateClubAddress(club.id, address, geoLocation)

        val updatedClub = repository.getClub(club.id)
        assertEquals(address, updatedClub?.address)
        assertEquals(geoLocation, updatedClub?.geoLocation)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating a club address for a non existing club`() {
        assertThrows(NotFoundException::class.java) {
            repository.updateClubAddress(UUID.randomUUID(), "new address", GeoLocation(10.1, 10.1))
        }
    }

    @Test
    fun `DocumentClubRepository should update club contacts`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val contacts = Contacts("new phone", Email("new-mail@test.com"))
        repository.updateClubContacts(club.id, contacts)

        assertEquals(contacts, repository.getClub(club.id)?.contacts)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating contacts for a non existing club`() {
        assertThrows(NotFoundException::class.java) {
            repository.updateClubContacts(
                UUID.randomUUID(), Contacts("new phone", Email("new-mail@test.com"))
            )
        }
    }

    @Test
    fun `DocumentClubRepository should update club average price`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val avgPrice = BigDecimal(15.5)
        repository.updateClubAvgPrice(club.id, avgPrice)

        assertEquals(avgPrice, repository.getClub(club.id)?.avgPrice)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating club abg price for a non existing club`() {
        assertThrows(NotFoundException::class.java) {
            repository.updateClubAvgPrice(UUID.randomUUID(), BigDecimal(15.5))
        }
    }

    @Test
    fun `DocumentClubRepository should update a club field`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val name = "new name"
        val isIndoor = false
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass
        repository.updateClubField(club.id, field.id, name, isIndoor, hasSand, wallsMaterial)

        val updatedField = repository.getClub(club.id)?.fields?.first()
        assertEquals(field.id, updatedField?.id)
        assertEquals(name, updatedField?.name)
        assertEquals(isIndoor, updatedField?.isIndoor)
        assertEquals(hasSand, updatedField?.hasSand)
        assertEquals(wallsMaterial, updatedField?.wallsMaterial)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating a club field for non existing field`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())
        val name = "new name"
        val isIndoor = false
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass

        assertThrows(NotFoundException::class.java) {
            repository.updateClubField(club.id, UUID.randomUUID(), name, isIndoor, hasSand, wallsMaterial)
        }
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating a club field for non existing club`() {
        val name = "new name"
        val isIndoor = false
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass

        assertThrows(NotFoundException::class.java) {
            repository.updateClubField(UUID.randomUUID(), UUID.randomUUID(), name, isIndoor, hasSand, wallsMaterial)
        }
    }

    @Test
    fun `DocumentClubRepository should add a field to a club`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())

        val newField = field.copy(id = UUID.randomUUID())
        repository.addFieldToClub(club.id, newField.name, newField.isIndoor, newField.hasSand, newField.wallsMaterial)

        val fields = repository.getClub(club.id)?.fields!!
        assertEquals(2, fields.size)
        assertTrue(fields.contains(field))
        assertTrue(fields.any { it.name == newField.name })
        assertTrue(fields.any { it.isIndoor == newField.isIndoor })
        assertTrue(fields.any { it.hasSand == newField.hasSand })
        assertTrue(fields.any { it.wallsMaterial == newField.wallsMaterial })
    }

    @Test
    fun `DocumentClubRepository should update a club availability`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val timeSlot = TimeSlot(startDateTime, startDateTime.plusHours(1))
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(LocalDate.now() to listOf(fieldAvailability)))

        repository.updateClubAvailability(club.id, availability)

        assertEquals(availability, repository.getClub(club.id)?.availability)
    }

    @Test
    fun `DocumentClubRepository should throw an exception when updating availability for a non existing club`() {
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val timeSlot = TimeSlot(startDateTime, startDateTime.plusHours(1))
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(LocalDate.now() to listOf(fieldAvailability)))
        assertThrows(NotFoundException::class.java) {
            repository.updateClubAvailability(UUID.randomUUID(), availability)
        }
    }

    @Test
    fun `DocumentClubRepository should add a field availability for a club`() {
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)
        col.save(club.toClubDTO())
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val timeSlot = TimeSlot(startDateTime, startDateTime.plusHours(1))
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)

        repository.addClubAvailability(club.id, fieldAvailability)

        assertTrue(
            repository.getClub(club.id)!!
                .availability
                .byDate
                .getOrDefault(startDateTime.toLocalDate(), emptyList())
                .any { it == fieldAvailability }
        )
    }

    @Test
    fun `DocumentClubRepository should return nearest clubs available for reservation`() {
        val today = LocalDate.now()
        val latitude = 41.386737
        val longitude = 2.170167
        val radius = 2.0
        val radiusUnit = RadiusUnit.Kilometers

        val farLatitude = 41.403966
        val farLongitude = 2.191595

        val nearLatitude = 41.393581
        val nearLongitude = 2.164756

        val nearGeoLocation = GeoLocation(nearLongitude, nearLatitude)
        val farGeoLocation = GeoLocation(farLongitude, farLatitude)

        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val timeSlot = TimeSlot(startDateTime, startDateTime.plusHours(1))
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(LocalDate.now() to listOf(fieldAvailability)))

        val nearClubNotAvailable = getClub(UUID.randomUUID()).copy(geoLocation = nearGeoLocation)
        val nearClubAvailable =
            getClub(UUID.randomUUID()).copy(geoLocation = nearGeoLocation, availability = availability)
        val farClubNotAvailable = getClub(UUID.randomUUID()).copy(geoLocation = farGeoLocation)
        val farClubAvailable =
            getClub(UUID.randomUUID()).copy(geoLocation = farGeoLocation, availability = availability)
        col.insertMany(
            listOf(nearClubAvailable, nearClubNotAvailable, farClubAvailable, farClubNotAvailable)
                .map { it.toClubDTO() }
        )

        val nearestClubsAvailableForReservation =
            repository.getNearestClubsAvailableForReservation(today, latitude, longitude, radius, radiusUnit)
        assertEquals(listOf(nearClubAvailable), nearestClubsAvailableForReservation)
    }

    @Test
    fun `DocumentClubRepository should return nearest clubs`() {
        val latitude = 41.386737
        val longitude = 2.170167
        val radius = 2.0
        val radiusUnit = RadiusUnit.Kilometers

        val farLatitude = 41.403966
        val farLongitude = 2.191595

        val nearLatitude = 41.393581
        val nearLongitude = 2.164756

        val nearGeoLocation = GeoLocation(nearLongitude, nearLatitude)
        val farGeoLocation = GeoLocation(farLongitude, farLatitude)

        val nearClub = getClub(UUID.randomUUID()).copy(geoLocation = nearGeoLocation)
        val farClub = getClub(UUID.randomUUID()).copy(geoLocation = farGeoLocation)
        col.insertMany(listOf(nearClub, farClub).map { it.toClubDTO() })

        val nearestClubs = repository.getNearestClubs(latitude, longitude, radius, radiusUnit)
        assertEquals(listOf(nearClub), nearestClubs)
    }
}
