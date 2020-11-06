package com.octawizard.server.input

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User

data class CreateUserInput(val email: Email, val name: String, val gender: Gender?, val phone: String?) {
    fun toUser(): User {
        return if (gender == null) {
            User(email, name, phone = phone)
        } else {
            User(email, name, gender, phone)
        }
    }
}


