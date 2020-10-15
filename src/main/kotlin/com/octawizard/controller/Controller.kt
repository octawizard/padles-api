package com.octawizard.controller

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.match.*
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import com.octawizard.server.input.OpType
import com.octawizard.server.input.PatchMatchInput
import io.ktor.features.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class Controller(
        private val createUser: CreateUser,
        private val createMatch: CreateMatch,
        private val deleteMatch: DeleteMatch,
        private val findAvailableMatches: FindAvailableMatches,
        private val getMatch: GetMatch,
        private val getUser: GetUser,
        private val joinMatch: JoinMatch,
        private val leaveMatch: LeaveMatch,
        private val updateUser: UpdateUser,
        private val deleteUser: DeleteUser
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

    suspend fun patchMatch(input: PatchMatchInput, matchId: UUID): Match? {
        val user = getUser(input.value) ?: throw NotFoundException("user ${input.value} not found")
        return when (input.op) {
            OpType.remove -> async { leaveMatch(user, matchId) }
            OpType.replace -> async { joinMatch(user, matchId) }
        }
    }

    suspend fun deleteMatch(matchId: UUID) {
        return async { deleteMatch.invoke(matchId) }
    }

    suspend fun deleteUser(email: Email) {
        return async { deleteUser.invoke(email) }
    }
}

suspend fun <T> async(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Default, block)
