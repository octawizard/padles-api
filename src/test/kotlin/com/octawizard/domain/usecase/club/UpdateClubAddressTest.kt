package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateClubAddressTest {

    @Test
    fun `UpdateClubAddress call repository to update club address and update all reservation of that club`() {
        val club = Club(UUID.randomUUID(), "", "", mockk(), emptySet(), EmptyAvailability, BigDecimal.ONE, mockk())
        val clubRepository = mockk<ClubRepository>(relaxed = true)
        val reservationRepository = mockk<ReservationRepository>(relaxed = true)
        val updateClubAddress = UpdateClubAddress(clubRepository, reservationRepository)
        val address = "new address"
        val location = GeoLocation(30.1, 30.1)

        val updatedClub = updateClubAddress.invoke(club, address, location)

        assertEquals(address, updatedClub.address)
        assertEquals(location, updatedClub.geoLocation)
        verify(exactly = 1) {
            clubRepository.updateClubAddress(club.id, address, location)
            Thread.sleep(100)
            reservationRepository.updateClubAddress(club.id, location)
        }
    }
}
