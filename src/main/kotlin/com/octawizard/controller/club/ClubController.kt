package com.octawizard.controller.club

import com.octawizard.controller.async
import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.WallsMaterial
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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class ClubController(
    private val getClub: GetClub,
    private val createClub: CreateClub,
    private val getNearestClubs: GetNearestClubs,
    private val updateClubName: UpdateClubName,
    private val updateClubAddress: UpdateClubAddress,
    private val updateClubContacts: UpdateClubContacts,
    private val updateClubAvgPrice: UpdateClubAvgPrice,
    private val addFieldToClub: AddFieldToClub,
    private val updateClubField: UpdateClubField,
    private val updateClubAvailability: UpdateClubAvailability,
) {

    suspend fun getClub(clubId: UUID): Club? {
        return async { getClub.invoke(clubId) }
    }

    suspend fun createClub(
        name: String,
        address: String,
        geoLocation: GeoLocation,
        avgPrice: BigDecimal,
        contacts: Contacts,
        fields: Set<Field>?,
        availability: Availability?,
    ): Club {
        return async { createClub.invoke(name, address, geoLocation, avgPrice, contacts, fields, availability) }
    }

    suspend fun getNearestClubs(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club> {
        return async { getNearestClubs.invoke(longitude, latitude, radius, radiusUnit) }
    }

    suspend fun getAvailableNearestClubs(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
        day: LocalDate,
    ): List<Club> {
        return async { getNearestClubs.invoke(longitude, latitude, radius, radiusUnit, day) }
    }

    suspend fun updateClubName(club: Club, name: String): Club {
        return async { updateClubName.invoke(club, name) }
    }

    suspend fun updateClubAddress(club: Club, address: String, location: GeoLocation): Club {
        return async { updateClubAddress.invoke(club, address, location) }
    }

    suspend fun updateClubContacts(club: Club, contacts: Contacts): Club {
        return async { updateClubContacts.invoke(club, contacts) }
    }

    suspend fun updateClubAvgPrice(club: Club, avgPrice: BigDecimal): Club {
        return async { updateClubAvgPrice.invoke(club, avgPrice) }
    }

    suspend fun addToClubFields(
        club: Club,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Club {
        return async { addFieldToClub(club, name, indoor, hasSand, wallsMaterial) }
    }

    suspend fun updateClubField(
        club: Club,
        fieldId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Club {
        return async { updateClubField.invoke(club, fieldId, name, indoor, hasSand, wallsMaterial) }
    }

    suspend fun updateClubAvailability(club: Club, availability: Availability): Club {
        return async { updateClubAvailability.invoke(club, availability) }
    }
}
