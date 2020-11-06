package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateClubContacts(private val clubRepository: ClubRepository) {

    operator fun invoke(club: Club, contacts: Contacts): Club {
        clubRepository.updateClubContacts(club.id, contacts)
        return club.copy(contacts = contacts)
    }
}
