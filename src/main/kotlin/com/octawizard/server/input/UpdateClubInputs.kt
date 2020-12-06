package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.GeoLocation
import java.math.BigDecimal

data class UpdateClubNameInput(val name: String)

data class UpdateClubAddressNameInput(val address: String, val location: GeoLocation)

data class UpdateClubContactsInput(val phone: String, val email: Email)

data class UpdateClubAvgPriceInput(val avgPrice: BigDecimal)
