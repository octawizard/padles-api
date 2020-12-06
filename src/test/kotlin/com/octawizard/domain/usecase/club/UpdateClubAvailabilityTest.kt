package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateClubAvailabilityTest {

    @Test
    fun `UpdateClubAvailability call repository to update club address and update all reservation of that club`() {
        val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)
        val club = Club(UUID.randomUUID(), "", "", mockk(), setOf(field), EmptyAvailability, BigDecimal.ONE, mockk())
        val repository = mockk<ClubRepository>(relaxed = true)
        val updateClubAvailability = UpdateClubAvailability(repository)

        val startDateTime = LocalDateTime.now().withHour(10).truncatedTo(ChronoUnit.MILLIS)
        val timeSlot = TimeSlot(startDateTime, startDateTime.plusHours(1))
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(LocalDate.now() to listOf(fieldAvailability)))

        val updatedClub = updateClubAvailability.invoke(club, availability)

        assertEquals(availability, updatedClub.availability)
        verify(exactly = 1) {
            repository.updateClubAvailability(club.id, availability)
        }
    }
}
