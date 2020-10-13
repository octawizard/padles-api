package com.octawizard.domain.model

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class FieldAvailability(val timeSlot: TimeSlot, val field: Field, val price: BigDecimal) : Serializable

data class TimeSlot(val startDateTime: LocalDateTime, val endDateTime: LocalDateTime): Serializable
