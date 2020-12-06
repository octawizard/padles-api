package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.repository.club.ClubRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateClubContactsTest {

    @Test
    fun `UpdateClubContacts call repository to update club contacts`() {
        val club = Club(UUID.randomUUID(), "", "", mockk(), emptySet(), EmptyAvailability, BigDecimal.ONE, mockk())
        val repository = mockk<ClubRepository>(relaxed = true)
        val updateClubContacts = UpdateClubContacts(repository)

        val newContacts = Contacts("12345", Email("update@padles.com"))
        val updatedClub = updateClubContacts.invoke(club, newContacts)

        assertEquals(newContacts, updatedClub.contacts)
        verify(exactly = 1) {
            repository.updateClubContacts(club.id, newContacts)
        }
    }
}
