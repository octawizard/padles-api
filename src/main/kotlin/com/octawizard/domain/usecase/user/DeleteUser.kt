package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.Email
import com.octawizard.repository.user.UserRepository

class DeleteUser(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: Email) = userRepository.deleteUser(email)
}
