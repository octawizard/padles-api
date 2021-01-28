package com.octawizard.server.input

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import java.math.BigDecimal

data class CreateClubInput(
    val name: String?,
    val address: String?,
    val geoLocation: GeoLocation?,
    val avgPrice: BigDecimal?,
    val contacts: Contacts?,
    val fields: Set<Field>?,
    val availability: Availability?,
) {

    fun sanitize(): CreateClubInputSanitized {
        checkNotNull(name) { "name cannot be null" }
        checkNotNull(address) { "address cannot be null" }
        checkNotNull(geoLocation) { "geoLocation cannot be null" }
        checkNotNull(avgPrice) { "avgPrice cannot be null" }
        checkNotNull(contacts) { "contacts cannot be null" }
        return CreateClubInputSanitized(name, address, geoLocation, avgPrice, contacts, fields, availability)
    }
}

data class CreateClubInputSanitized(
    val name: String,
    val address: String,
    val geoLocation: GeoLocation,
    val avgPrice: BigDecimal,
    val contacts: Contacts,
    val fields: Set<Field>?,
    val availability: Availability?,
)
