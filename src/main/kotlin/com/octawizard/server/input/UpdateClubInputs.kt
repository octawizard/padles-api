package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.GeoLocation
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class UpdateClubNameInput(val name: String)

@Serializable
data class UpdateClubAddressNameInput(val address: String, val location: GeoLocation)

@Serializable
data class UpdateClubContactsInput(val phone: String, val email: Email)

@Serializable
data class UpdateClubAvgPriceInput(@Contextual val avgPrice: BigDecimal)
