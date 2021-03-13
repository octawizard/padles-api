package com.octawizard.server.input

import com.octawizard.domain.model.Email
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class CreateReservationInput(
    val reservedBy: Email,
    @Contextual val clubId: UUID,
    @Contextual val fieldId: UUID,
    @Contextual val startTime: LocalDateTime,
    @Contextual val endTime: LocalDateTime,
    val matchEmailPlayer2: Email?,
    val matchEmailPlayer3: Email?,
    val matchEmailPlayer4: Email?,
)
