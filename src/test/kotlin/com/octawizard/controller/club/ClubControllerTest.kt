package com.octawizard.controller.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.domain.usecase.club.AddFieldToClub
import com.octawizard.domain.usecase.club.CreateClub
import com.octawizard.domain.usecase.club.GetClub
import com.octawizard.domain.usecase.club.GetNearestClubs
import com.octawizard.domain.usecase.club.SearchClubsByName
import com.octawizard.domain.usecase.club.UpdateClubAddress
import com.octawizard.domain.usecase.club.UpdateClubAvailability
import com.octawizard.domain.usecase.club.UpdateClubAvgPrice
import com.octawizard.domain.usecase.club.UpdateClubContacts
import com.octawizard.domain.usecase.club.UpdateClubField
import com.octawizard.domain.usecase.club.UpdateClubName
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClubControllerTest {
    private val getClub: GetClub = mockk()
    private val createClub: CreateClub = mockk()
    private val getNearestClubs: GetNearestClubs = mockk()
    private val updateClubName: UpdateClubName = mockk()
    private val updateClubAddress: UpdateClubAddress = mockk()
    private val updateClubContacts: UpdateClubContacts = mockk()
    private val updateClubAvgPrice: UpdateClubAvgPrice = mockk()
    private val addFieldToClub: AddFieldToClub = mockk()
    private val updateClubField: UpdateClubField = mockk()
    private val updateClubAvailability: UpdateClubAvailability = mockk()
    private val searchClubsByName: SearchClubsByName = mockk()
    private val controller = ClubController(
        getClub,
        createClub,
        getNearestClubs,
        updateClubName,
        updateClubAddress,
        updateClubContacts,
        updateClubAvgPrice,
        addFieldToClub,
        updateClubField,
        updateClubAvailability,
        searchClubsByName,
    )

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `ClubController should call GetClub to get a club`() {
        val club = mockk<Club>()
        val clubId = UUID.randomUUID()
        every { getClub.invoke(any()) } returns club

        assertEquals(club, runBlocking { controller.getClub(clubId) })
        verify { getClub.invoke(clubId) }
    }

    @Test
    fun `ClubController should call CreateClub to create a club`() {
        val club = mockk<Club>()
        every { createClub.invoke(any(), any(), any(), any(), any(), any(), any()) } returns club
        val name = ""
        val address = ""
        val location = GeoLocation(1.1, 0.5)
        val avgPrice = BigDecimal.TEN
        val contacts = mockk<Contacts>()
        val fields = emptySet<Field>()
        val availability = EmptyAvailability


        assertEquals(club,
            runBlocking { controller.createClub(name, address, location, avgPrice, contacts, fields, availability) })
        verify { createClub.invoke(name, address, location, avgPrice, contacts, fields, availability) }
    }

    @Test
    fun `ClubController should call GetNearestClubs to get a list of near clubs`() {
        val clubs = listOf(mockk<Club>())
        every { getNearestClubs.invoke(any(), any(), any(), any()) } returns clubs

        assertEquals(
            clubs, runBlocking { controller.getNearestClubs(1.0, 1.0, 1.0, RadiusUnit.Miles) }
        )
        verify { getNearestClubs.invoke(1.0, 1.0, 1.0, RadiusUnit.Miles) }
    }

    @Test
    fun `ClubController should call GetNearestClubs to get a list of near available clubs`() {
        val clubs = listOf(mockk<Club>())
        every { getNearestClubs.invoke(any(), any(), any(), any(), any()) } returns clubs

        assertEquals(
            clubs,
            runBlocking { controller.getAvailableNearestClubs(1.0, 1.0, 1.0, RadiusUnit.Miles, LocalDate.now()) }
        )
        verify { getNearestClubs.invoke(1.0, 1.0, 1.0, RadiusUnit.Miles, LocalDate.now()) }
    }

    @Test
    fun `ClubController should call UpdateClubName to update the club's name`() {
        val club = mockk<Club>()
        val name = "new name"
        every { updateClubName.invoke(any(), any()) } returns club

        assertEquals(club, runBlocking { controller.updateClubName(club, name) })
        verify { updateClubName.invoke(club, name) }
    }

    @Test
    fun `ClubController should call UpdateClubAddress to update the club's address`() {
        val club = mockk<Club>()
        val address = "new name"
        val location = GeoLocation(44.1, 44.2)
        every { updateClubAddress.invoke(any(), any(), any()) } returns club

        assertEquals(club, runBlocking { controller.updateClubAddress(club, address, location) })
        verify { updateClubAddress.invoke(club, address, location) }
    }

    @Test
    fun `ClubController should call UpdateClubContacts to update the club's contacts`() {
        val club = mockk<Club>()
        val clubContacts = Contacts("123456", Email("club@padles.com"))
        every { updateClubContacts.invoke(any(), any()) } returns club

        assertEquals(club, runBlocking { controller.updateClubContacts(club, clubContacts) })
        verify { updateClubContacts.invoke(club, clubContacts) }
    }

    @Test
    fun `ClubController should call UpdateClubAvgPrice to update the club's average price`() {
        val club = mockk<Club>()
        val avgPrice = BigDecimal(15)
        every { updateClubAvgPrice.invoke(any(), any()) } returns club

        assertEquals(club, runBlocking { controller.updateClubAvgPrice(club, avgPrice) })
        verify { updateClubAvgPrice.invoke(club, avgPrice) }
    }

    @Test
    fun `ClubController should call AddFieldToClub to add a new field to the club's field list`() {
        val club = mockk<Club>()
        val name = "field"
        val indoor = true
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass
        every { addFieldToClub.invoke(any(), any(), any(), any(), any()) } returns club

        assertEquals(club, runBlocking { controller.addToClubFields(club, name, indoor, hasSand, wallsMaterial) })
        verify { addFieldToClub.invoke(club, name, indoor, hasSand, wallsMaterial) }
    }

    @Test
    fun `ClubController should call UpdateClubField to update a club's field`() {
        val club = mockk<Club>()
        val fieldId = UUID.randomUUID()
        val name = "field"
        val indoor = true
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass
        every { updateClubField.invoke(any(), any(), any(), any(), any(), any()) } returns club

        assertEquals(club,
            runBlocking { controller.updateClubField(club, fieldId, name, indoor, hasSand, wallsMaterial) })
        verify { updateClubField.invoke(club, fieldId, name, indoor, hasSand, wallsMaterial) }
    }

    @Test
    fun `ClubController should call UpdateClubAvailability to update club's fields availability`() {
        val club = mockk<Club>()
        every { updateClubAvailability.invoke(any(), any()) } returns club

        assertEquals(club, runBlocking { controller.updateClubAvailability(club, EmptyAvailability) })
        verify { updateClubAvailability.invoke(club, EmptyAvailability) }
    }

    @Test
    fun `ClubController should call SearchClubsByName to search clubs given a name`() {
        val club = mockk<Club>()
        every { searchClubsByName.invoke(any()) } returns listOf(club)

        val name = "search"
        assertEquals(listOf(club), runBlocking { controller.searchClubsByName(name) })
        verify { searchClubsByName.invoke(name) }
    }
}
