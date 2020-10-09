package com.octawizard.domain.model

import java.io.Serializable
import java.time.LocalDateTime

data class User(val email: Email, val name: String, val gender: Gender = Gender.male, val createdAt: LocalDateTime = LocalDateTime.now()): Serializable

enum class Gender: Serializable {
    male,
    female,
    other
}
