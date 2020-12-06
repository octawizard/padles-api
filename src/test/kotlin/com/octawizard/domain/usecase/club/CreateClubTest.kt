package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.repository.club.ClubRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateClubTest {

    @Test
    fun `CreateClub call repository to create a club with fields and availability`() {
        val club = mockk<Club>()
        val repository = mockk<ClubRepository>(relaxed = true)
        val createClub = CreateClub(repository)
        val name = "name"
        val address = "addres"
        val location = GeoLocation(0.1, 0.2)
        val avgPrice = BigDecimal.ZERO
        val contacts = Contacts("12345", Email("club@test.com"))
        val fields = emptySet<Field>()
        val availability = EmptyAvailability

        every { repository.createClub(name, address, location, avgPrice, contacts, fields, availability) } returns club

        assertEquals(club, createClub.invoke(name, address, location, avgPrice, contacts, fields, availability))
        verify(exactly = 1) { repository.createClub(name, address, location, avgPrice, contacts, fields, availability) }
    }

    @Test
    fun `CreateClub call repository to create a club without fields and availability`() {
        val club = mockk<Club>()
        val repository = mockk<ClubRepository>(relaxed = true)
        val createClub = CreateClub(repository)
        val name = "name"
        val address = "addres"
        val location = GeoLocation(0.1, 0.2)
        val avgPrice = BigDecimal.ZERO
        val contacts = Contacts("12345", Email("club@test.com"))
        val fields = emptySet<Field>()
        val availability = EmptyAvailability

        every { repository.createClub(name, address, location, avgPrice, contacts, fields, availability) } returns club

        assertEquals(club, createClub.invoke(name, address, location, avgPrice, contacts, null, null))
        verify(exactly = 1) { repository.createClub(name, address, location, avgPrice, contacts, fields, availability) }
    }
}
