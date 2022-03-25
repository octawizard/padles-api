package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.repository.club.ClubRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetNearestClubsTest {
    private val repository = mockk<ClubRepository>(relaxed = true)
    private val getNearestClubs = GetNearestClubs(repository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `GetNearestClubs call repository to get nearest clubs given a position`() {
        val club = mockk<Club>()
        val longitude = 1.1
        val latitude = 2.2
        val radius = 1.5
        val radiusUnit = RadiusUnit.Miles

        every {
            repository.getNearestClubs(longitude, latitude, radius, radiusUnit)
        } returns listOf(club)

        assertEquals(listOf(club), getNearestClubs.invoke(longitude, latitude, radius, radiusUnit))
        verify(exactly = 1) {
            repository.getNearestClubs(longitude, latitude, radius, radiusUnit)
        }
    }

    @Test
    fun `GetNearestClubs call repository to get nearest clubs available for reservation given a position`() {
        val club = mockk<Club>()
        val longitude = 1.1
        val latitude = 2.2
        val radius = 1.5
        val radiusUnit = RadiusUnit.Miles
        val day = LocalDate.now()

        every {
            repository.getNearestClubsAvailableForReservation(day, longitude, latitude, radius, radiusUnit)
        } returns listOf(club)

        assertEquals(listOf(club), getNearestClubs.invoke(longitude, latitude, radius, radiusUnit, day))
        verify(exactly = 1) {
            repository.getNearestClubsAvailableForReservation(day, longitude, latitude, radius, radiusUnit)
        }
    }
}
