package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User

data class UserUpdateInput(val name: String, val gender: Gender, val phone: String) {
    fun toUser(email: Email): User = User(email, name, gender, phone)
}
