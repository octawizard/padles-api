package com.octawizard.domain.model

import java.time.LocalDateTime

data class User(val email: Email, val name: String, val createdAt: LocalDateTime = LocalDateTime.now())
