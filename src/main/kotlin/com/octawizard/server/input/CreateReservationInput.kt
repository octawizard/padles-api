package com.octawizard.server.input

import com.octawizard.domain.model.Email
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class CreateReservationInput(
    val reservedBy: Email, val clubId: UUID,
    val fieldId: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val price: BigDecimal,
    val matchEmailPlayer2: Email?,
    val matchEmailPlayer3: Email?,
    val matchEmailPlayer4: Email?,
)
