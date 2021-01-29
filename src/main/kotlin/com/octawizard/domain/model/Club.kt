package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Serializable
data class Club(
    @Contextual val id: UUID,
    val name: String,
    val address: String,
    val geoLocation: GeoLocation,
    val fields: Set<Field>,
    val availability: Availability,
    @Contextual val avgPrice: BigDecimal,
    val contacts: Contacts,
) {
    init {
        check(
            availability.byDate.values
                .asSequence()
                .flatten()
                .map { it.field.id }
                .distinct()
                .all { hasField(it) }
        ) {
            "only club fields can be available, please review available fields"
        }
    }

    private fun hasField(fieldId: UUID): Boolean {
        return fields.any { it.id == fieldId }
    }
}

@Serializable
data class Availability(val byDate: Map<@Contextual LocalDate, List<FieldAvailability>>) {
    init {
        check(byDate.entries.all { (date, availableFields) -> dateMatchesTimeSlot(availableFields, date) }) {
            "field availability timeslot is not matching the availability date"
        }
    }

    private fun dateMatchesTimeSlot(
        availableFields: List<FieldAvailability>,
        date: LocalDate,
    ) = availableFields.all { date == it.timeSlot.startDateTime.toLocalDate() }
}

val EmptyAvailability = Availability(emptyMap())

@Serializable
data class Contacts(val phone: String, val email: Email)
