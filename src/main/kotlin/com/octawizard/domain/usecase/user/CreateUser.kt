package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.User
import com.octawizard.repository.UserRepository

class CreateUser(private val userRepository: UserRepository) {

    operator fun invoke(user: User): User = userRepository.createUser(user)
}
