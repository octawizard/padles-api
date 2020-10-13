package com.octawizard.domain.model

import java.util.*

data class Field(val id: UUID, val isIndoor: Boolean, val wallsMaterial: WallsMaterial, val name: String?)

enum class WallsMaterial {
    Glass, Bricks
}
