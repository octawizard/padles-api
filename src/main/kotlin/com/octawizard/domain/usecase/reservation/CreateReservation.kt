package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.*
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.user.UserRepository
import com.octawizard.server.route.entityNotFound
import io.ktor.features.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class CreateReservation(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository
) {

    operator fun invoke(
        reservedBy: Email,
        clubId: UUID,
        fieldId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        matchEmailPlayer2: Email?,
        matchEmailPlayer3: Email?,
        matchEmailPlayer4: Email?
    ): Reservation {

        val reservationOwner = userRepository.getUser(reservedBy) ?: userNotFound(reservedBy)
        val club = clubRepository.getClub(clubId) ?: clubNotFound(clubId)
        val field = club.fields.find { it.id == fieldId } ?: fieldNotFound(fieldId)

        val player2 = matchEmailPlayer2?.let { userRepository.getUser(reservedBy) }
        val player3 = matchEmailPlayer3?.let { userRepository.getUser(reservedBy) }
        val player4 = matchEmailPlayer4?.let { userRepository.getUser(reservedBy) }

        val match = Match(listOfNotNull(reservationOwner, player2, player3, player4))

        val preReservedAvailability: FieldAvailability? = club.availability.byDate[startTime.toLocalDate()]
            ?.find { it.timeSlot.startDateTime == startTime && it.timeSlot.endDateTime == endTime && it.field.id == fieldId }
            ?: entityNotFound<FieldAvailability>("field=$fieldId from=$startTime to=$endTime")
        val updatedAvailability = Availability(
            club.availability.byDate.filterNot { (day, fieldsAv) ->
                day == startTime.toLocalDate() && fieldsAv.contains(preReservedAvailability)
            }
        )
        clubRepository.updateClubAvailability(clubId, updatedAvailability)

        val clubReservationInfo = ClubReservationInfo(club.id, club.name, field, club.geoLocation)

        return reservationRepository.createReservation(
            reservationOwner, clubReservationInfo, startTime, endTime, price, match
        )

        //todo in case of error we need to restore the availability for the club. Maybe instead of removing it we can
        // have a boolean inReservation=true to mark them not available. Would be easy to restore it in case
    }

    private fun userNotFound(email: Email): User {
        throw NotFoundException("user ${email.value} not found")
    }

    private fun clubNotFound(id: UUID): Club {
        throw NotFoundException("club $id not found")
    }

    private fun fieldNotFound(id: UUID): Field {
        throw NotFoundException("field $id not found")
    }
}
