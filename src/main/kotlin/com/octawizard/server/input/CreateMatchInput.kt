package com.octawizard.server.input

import com.octawizard.domain.model.Email

data class CreateMatchInput(val player1: Email, val player2: Email?, val player3: Email?, val player4: Email?)
