package com.octawizard.server.input

import com.octawizard.domain.model.Availability
import kotlinx.serialization.Serializable

@Serializable
data class UpdateClubAvailabilityInput(val availability: Availability)
