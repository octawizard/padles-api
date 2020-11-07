package com.octawizard.controller

import com.octawizard.domain.model.*
import com.octawizard.domain.usecase.club.UpdateClubAvailability
import com.octawizard.domain.usecase.club.*
import com.octawizard.domain.usecase.reservation.CancelReservation
import com.octawizard.domain.usecase.reservation.CreateReservation
import com.octawizard.domain.usecase.reservation.GetNearestAvailableReservations
import com.octawizard.domain.usecase.reservation.GetReservation
import com.octawizard.domain.usecase.reservation.JoinMatch
import com.octawizard.domain.usecase.reservation.LeaveMatch
import com.octawizard.domain.usecase.reservation.UpdateMatchResult
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import com.octawizard.server.input.OpType
import com.octawizard.server.input.PatchMatchInput
import io.ktor.features.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Controller(
    // User
    private val createUser: CreateUser,
    private val updateUser: UpdateUser,
    private val deleteUser: DeleteUser,
    private val getUser: GetUser,
    // Reservation
    private val createReservation: CreateReservation,
    private val cancelReservation: CancelReservation,
    private val getReservation: GetReservation,
    private val getNearestAvailableReservations: GetNearestAvailableReservations,
    // Reservation Match
    private val updateMatchResult: UpdateMatchResult,
    private val joinMatch: JoinMatch,
    private val leaveMatch: LeaveMatch,
    // Club
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

    suspend fun createUser(user: User): User = async { createUser.invoke(user) }

    suspend fun getUser(emailString: String): User? {
        val email = Email(emailString)
        return async { getUser(email) }
    }

    suspend fun updateUser(updated: User): User? {
        return async {
            getUser(updated.email)?.email?.let { updateUser.invoke(updated) }
        }
    }

    suspend fun deleteUser(email: Email) {
        return async { deleteUser.invoke(email) }
    }

    suspend fun getReservation(reservationId: UUID): Reservation? {
        return async { getReservation.invoke(reservationId) }
    }

    suspend fun createReservation(
        reservedBy: Email,
        clubId: UUID,
        fieldId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        matchEmailPlayer2: Email?,
        matchEmailPlayer3: Email?,
        matchEmailPlayer4: Email?,
    ): Reservation {
        return async {
            createReservation.invoke(
                reservedBy, clubId, fieldId, startTime, endTime, price, matchEmailPlayer2,
                matchEmailPlayer3, matchEmailPlayer4
            )
        }
    }

    suspend fun cancelReservation(reservation: Reservation): Reservation {
        return async { cancelReservation.invoke(reservation.id) }
    }

    suspend fun updateReservationMatchResult(reservation: Reservation, matchResult: MatchResult): Reservation {
        return async { updateMatchResult.invoke(reservation, matchResult) }
    }

    suspend fun getNearestAvailableReservations(
        longitude: Double, latitude: Double, radius: Double, radiusUnit: RadiusUnit,
    ): List<Reservation> {
        return async { getNearestAvailableReservations.invoke(longitude, latitude, radius, radiusUnit) }
    }

    suspend fun patchReservationMatch(input: PatchMatchInput, reservation: Reservation): Reservation {
        val user = getUser(input.value) ?: throw NotFoundException("user ${input.value} not found")
        return when (input.op) {
            OpType.remove -> async { leaveMatch(user, reservation) }
            OpType.replace -> async { joinMatch(user, reservation) }
        }
    }

    suspend fun getClub(clubId: UUID): Club? {
        return async { getClub.invoke(clubId) }
    }

    suspend fun createClub(
        name: String,
        address: String,
        geoLocation: GeoLocation,
        avgPrice: BigDecimal,
        contacts: Contacts,
        fields: List<Field>?,
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

suspend fun <T> async(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Default, block)
