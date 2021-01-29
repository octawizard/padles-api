package com.octawizard.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoLocation(val longitude: Double, val latitude: Double)
