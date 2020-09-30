package com.octawizard.domain.model

import java.math.BigDecimal
import java.util.*

data class Club(
    val id: UUID,
    val name: String,
    val address: String,
    val fieldsCount: Int,
    val avgPrice: BigDecimal
)
