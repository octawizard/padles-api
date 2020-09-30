package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User

data class UserUpdateInput(val name: String)

fun UserUpdateInput.toUser(email: Email): User = User(email, name)
