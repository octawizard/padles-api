package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.User
import com.octawizard.repository.user.UserRepository

class UpdateUser(private val userRepository: UserRepository) {

    operator fun invoke(user: User): User = userRepository.updateUser(user)
}
