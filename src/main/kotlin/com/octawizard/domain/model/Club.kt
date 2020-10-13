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
        val fields: List<Field>,
        val availability: Availability,
        val avgPrice: BigDecimal,
        val contacts: Contacts
)

data class Availability(val byDate: Map<LocalDate, List<FieldAvailability>>): Serializable

data class Contacts(val phone: String, val email: Email) : Serializable
