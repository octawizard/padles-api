package com.octawizard.server.input

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateClubInput(
    val name: String,
    val address: String,
    val geoLocation: GeoLocation,
    @Contextual val avgPrice: BigDecimal,
    val contacts: Contacts,
    val fields: Set<Field>?,
    val availability: Availability?,
)
