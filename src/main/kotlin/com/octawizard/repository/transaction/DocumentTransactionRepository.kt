package com.octawizard.repository.transaction

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.client.MongoCollection
import com.mongodb.client.TransactionBody
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.User
import com.octawizard.repository.MongoSessionProvider
import com.octawizard.repository.club.model.AvailabilityDTO
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.club.model.DateFormatter
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toClubReservationInfoDTO
import com.octawizard.repository.reservation.model.toMatchDTO
import com.octawizard.repository.reservation.model.toReservation
import mu.KotlinLogging
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.keyProjection
import org.litote.kmongo.property.KPropertyPath
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.save
import org.litote.kmongo.unset
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class DocumentTransactionRepository(
    private val clubs: MongoCollection<ClubDTO>,
    private val reservations: MongoCollection<ReservationDTO>,
    private val sessionProvider: MongoSessionProvider,
) : TransactionRepository {
    private val logger = KotlinLogging.logger { }

    override fun createReservation(
        reservationOwner: User,
        clubReservationInfo: ClubReservationInfo,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        match: Match,
    ): Reservation {
        val clientSession = sessionProvider.startClientSession()

        val txnOptions: TransactionOptions = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.LOCAL)
            .writeConcern(WriteConcern.MAJORITY)
            .build()

        /* Important:: mandatory to pass the session to all the operations. */
        val txnBody: TransactionBody<Reservation> = TransactionBody {

            val startTimeString = startTime.toLocalDate().format(DateFormatter)
            val availabilityByDayProperty: KPropertyPath<out Any?, List<FieldAvailability>?> =
                (ClubDTO::availability / AvailabilityDTO::byDate).keyProjection(startTimeString)

            val updateResult = clubs.updateOne(
                clientSession,
                ClubDTO::id eq clubReservationInfo.clubId,
                pullByFilter(
                    availabilityByDayProperty,
                    and(
                        FieldAvailability::timeSlot / TimeSlot::startDateTime eq startTime,
                        FieldAvailability::timeSlot / TimeSlot::endDateTime eq endTime,
                        (FieldAvailability::field / Field::id) eq clubReservationInfo.field.id
                    )
                )
            )

            if (updateResult.modifiedCount == 0L) {
                throw IllegalArgumentException(
                    "Cannot reserve: field=${clubReservationInfo.field.id} from=$startTime to=$endTime"
                )
            }

            // remove entry(s) for day availability in case the list of FieldAvailability is null or empty now
            val club = clubs.findOneById(clientSession, clubReservationInfo.clubId)!!
            val keysToUnset = club.availability.byDate.filterValues { it.isNullOrEmpty() }.keys
            if (keysToUnset.isNotEmpty()) {
                val unsets: List<Bson> = keysToUnset.map {
                    unset((ClubDTO::availability / AvailabilityDTO::byDate).keyProjection(it))
                }
                clubs.updateMany(clientSession, ClubDTO::id eq clubReservationInfo.clubId, and(unsets))
            }

            val reservation = ReservationDTO(
                UUID.randomUUID(),
                match.toMatchDTO(),
                clubReservationInfo.toClubReservationInfoDTO(),
                startTime,
                endTime,
                reservationOwner,
                price,
                ReservationStatus.Pending,
                PaymentStatus.PendingPayment
            )
            reservations.save(clientSession, reservation)
            reservation.toReservation()
        }

        try {
            return clientSession.withTransaction(txnBody, txnOptions)
        } catch (e: RuntimeException) {
            logger.error("Failed to create reservation: ${e.message}", e)
            throw e
        } finally {
            clientSession.close()
        }
    }
}
