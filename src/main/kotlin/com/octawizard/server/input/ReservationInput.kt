package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class ReservationInput(
        val reservedBy: Email,
        val clubId: UUID,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val price: BigDecimal,
        val match: Match
)
