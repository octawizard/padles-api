package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User

data class UserInput(val email: Email, val name: String)

fun UserInput.toUser(): User = User(email, name)
