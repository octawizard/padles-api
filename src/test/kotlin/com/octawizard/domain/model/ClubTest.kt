package com.octawizard.domain.model

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClubTest {

    @Test
    fun `Club should throw exception when creating it if availability include fields that are not owned by the club`() {
        val clubField = Field(UUID.randomUUID(), "club field", true, WallsMaterial.Glass)
        val anotherField = Field(UUID.randomUUID(), "not club field", true, WallsMaterial.Glass)
        val wrongFieldAvailability = listOf(
            FieldAvailability(TimeSlot(LocalDateTime.now(), LocalDateTime.now()), anotherField, BigDecimal.TEN)
        )
        assertThrows(IllegalStateException::class.java) {
            Club(
                UUID.randomUUID(),
                "test club",
                "club address",
                mockk(),
                listOf(clubField),
                Availability(mapOf(LocalDate.now() to wrongFieldAvailability)),
                BigDecimal.TEN,
                mockk()
            )
        }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvailabilityTest {

    @Test
    fun `Availability should throw exception when creating it if date and timeslot dates are not matching`() {
        val fieldAvailability = listOf(
            FieldAvailability(TimeSlot(LocalDateTime.now(), LocalDateTime.now()), mockk(), BigDecimal.TEN)
        )
        assertThrows(IllegalStateException::class.java) {
            Availability(mapOf(LocalDate.now().minusDays(1) to fieldAvailability))
        }
    }
}
