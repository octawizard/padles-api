package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Field(
    @Contextual val id: UUID,
    val name: String,
    val isIndoor: Boolean,
    val wallsMaterial: WallsMaterial,
    val hasSand: Boolean = true,
)

enum class WallsMaterial {
    Glass, Bricks
}
