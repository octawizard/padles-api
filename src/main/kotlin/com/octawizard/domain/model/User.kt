package com.octawizard.domain.model

import java.io.Serializable
import java.time.LocalDateTime

data class User(
        val email: Email,
        val name: String,
        val gender: Gender = Gender.Other,
        val phone: String? = null,
        val createdAt: LocalDateTime = LocalDateTime.now()
) : Serializable

enum class Gender : Serializable {
    Male,
    Female,
    Other
}
