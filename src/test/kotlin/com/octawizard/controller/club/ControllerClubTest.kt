package com.octawizard.controller.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.usecase.club.AddFieldToClub
import com.octawizard.domain.usecase.club.CreateClub
import com.octawizard.domain.usecase.club.GetClub
import com.octawizard.domain.usecase.club.GetNearestClubs
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
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerClubTest {
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

}
