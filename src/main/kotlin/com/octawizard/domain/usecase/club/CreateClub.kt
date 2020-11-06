package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.repository.club.ClubRepository
import java.math.BigDecimal

class CreateClub(private val clubRepository: ClubRepository) {

    operator fun invoke(
        name: String,
        address: String,
        geoLocation: GeoLocation,
        avgPrice: BigDecimal,
        contacts: Contacts,
        fields: List<Field>?,
        availability: Availability?,
    ): Club {
        return clubRepository.createClub(
            name,
            address,
            geoLocation,
            avgPrice,
            contacts,
            fields ?: emptyList(),
            availability ?: EmptyAvailability
        )
    }
}
