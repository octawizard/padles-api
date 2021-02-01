package com.octawizard.server.input

import com.octawizard.domain.model.WallsMaterial
import kotlinx.serialization.Serializable

@Serializable
data class AddClubFieldInput(
    val name: String,
    val isIndoor: Boolean,
    val wallsMaterial: WallsMaterial,
    val hasSand: Boolean = true,
)
