package com.octawizard.controller

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.match.CreateMatch
import com.octawizard.domain.usecase.match.FindAvailableMatches
import com.octawizard.domain.usecase.match.GetMatch
import com.octawizard.domain.usecase.match.JoinMatch
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class Controller(
    private val createUser: CreateUser,
    private val createMatch: CreateMatch,
    private val findAvailableMatches: FindAvailableMatches,
    private val getMatch: GetMatch,
    private val getUser: GetUser,
    private val joinMatch: JoinMatch,
    private val updateUser: UpdateUser
) {

    suspend fun createUser(user: User): User = async { createUser.invoke(user) }

    suspend fun getUser(emailString: String): User? {
        val email = Email(emailString)
        return async { getUser(email) }
    }

    suspend fun updateUser(updated: User): User? {
        return async {
            getUser(updated.email)?.email?.let { updateUser.invoke(updated) }
        }
    }

    suspend fun createMatch(player1: Email, player2: Email?, player3: Email?, player4: Email?): Match {
        return async { createMatch.invoke(player1, player2, player3, player4) }
    }

    suspend fun getAllAvailableMatches(): List<Match> {
        return async { findAvailableMatches() }
    }

    suspend fun getMatch(inputMatchId: UUID): Match? {
        return async { getMatch.invoke(inputMatchId) }
    }
}

suspend fun <T> async(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Default, block)
