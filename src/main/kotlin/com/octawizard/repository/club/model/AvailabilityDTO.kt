package com.octawizard.repository.club.model

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.FieldAvailability
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val DateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

data class AvailabilityDTO(val byDate: Map<String, List<FieldAvailability>>) {
    fun toAvailability(): Availability = Availability(byDate.mapKeys { LocalDate.parse(it.key, DateFormatter) })
}

fun Availability.toAvailabilityDTO(): AvailabilityDTO = AvailabilityDTO(byDate.mapKeys { it.key.format(DateFormatter) })
