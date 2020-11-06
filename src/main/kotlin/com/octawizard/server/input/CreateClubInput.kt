package com.octawizard.server.input

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import java.math.BigDecimal

data class CreateClubInput(
    val name: String,
    val address: String,
    val geoLocation: GeoLocation,
    val avgPrice: BigDecimal,
    val contacts: Contacts,
    val fields: List<Field>?,
    val availability: Availability?,
)
