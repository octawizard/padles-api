package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.user.UserRepository

class GetUser(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: Email): User? = userRepository.getUser(email)
}
