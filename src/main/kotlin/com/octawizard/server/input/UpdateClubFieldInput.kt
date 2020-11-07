package com.octawizard.server.input

import com.octawizard.domain.model.WallsMaterial

data class UpdateClubFieldInput(
    val name: String,
    val isIndoor: Boolean,
    val wallsMaterial: WallsMaterial,
    val hasSand: Boolean = true,
)
