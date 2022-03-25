package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class User(
    val email: Email,
    val name: String,
    val gender: Gender = Gender.Other,
    val phone: String? = null,
    @Contextual val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class Gender {
    Male,
    Female,
    Other
}
