package com.octawizard.domain.model

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class Club(
    val id: UUID,
    val name: String,
    val address: String,
    val geoLocation: GeoLocation,
    val fields: Set<Field>,
    val availability: Availability,
    val avgPrice: BigDecimal,
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

data class Availability(val byDate: Map<LocalDate, List<FieldAvailability>>) : Serializable {
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

data class Contacts(val phone: String, val email: Email) : Serializable
