package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class FieldAvailability(val timeSlot: TimeSlot, val field: Field, @Contextual val price: BigDecimal)

@Serializable
data class TimeSlot(@Contextual val startDateTime: LocalDateTime, @Contextual val endDateTime: LocalDateTime)
