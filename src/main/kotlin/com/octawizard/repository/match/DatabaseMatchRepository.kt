package com.octawizard.repository.match

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.user.Users
import com.octawizard.repository.user.UsersEntity
import io.ktor.features.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.*

class DatabaseMatchRepository : MatchRepository {

    override fun createMatch(user1: User, user2: User?, user3: User?, user4: User?): Match {
        val now = LocalDateTime.now()
        val draftStatus = MatchStatus.Draft
        val matchId = transaction {
            Matches.insertAndGetId {
                it[player1] = EntityID(user1.email.value, Users)
                it[player2] = user2?.let { u -> EntityID(u.email.value, Users) }
                it[player3] = user3?.let { u -> EntityID(u.email.value, Users) }
                it[player4] = user4?.let { u -> EntityID(u.email.value, Users) }
                it[createdAt] = now
                it[status] = draftStatus
                it[reservation] = null
            }
        }
        return Match(matchId.value, user1, user2, user3, user4, now, draftStatus, null)
    }

    override fun getMatch(id: UUID): Match? {
        return transaction {
            MatchesEntity.findById(id)?.toMatch()
        }
    }

    override fun joinMatch(user: User, matchId: UUID) {
        return transaction {
            val players: Players = Matches.select { Matches.id eq matchId }
                .forUpdate()
                .map {
                    Players(
                        it[Matches.player1].value,
                        it[Matches.player2]?.value,
                        it[Matches.player3]?.value,
                        it[Matches.player4]?.value
                    )
                }
                .firstOrNull() ?: throw NotFoundException()

            val updatedPlayers = addUserToMatch(players, user.email, matchId)

            Matches.update({ Matches.id eq matchId }) {
                it[player1] = EntityID(updatedPlayers.player1, Users)
                it[player2] = updatedPlayers.player2?.let { email -> EntityID(email, Users) }
                it[player3] = updatedPlayers.player3?.let { email -> EntityID(email, Users) }
                it[player4] = updatedPlayers.player4?.let { email -> EntityID(email, Users) }
            }

        }
    }

    private fun addUserToMatch(players: Players, userEmail: Email, matchId: UUID): Players {
        return when {
            players.player2 == null -> players.copy(player2 = userEmail.value)
            players.player3 == null -> players.copy(player3 = userEmail.value)
            players.player4 == null -> players.copy(player4 = userEmail.value)
            else -> throw IllegalStateException("match $matchId is full, cannot add player")
        }
    }

    override fun allAvailableMatches(): List<Match> {
        return transaction {
            MatchesEntity.find { Matches.player2.isNull() or Matches.player3.isNull() or Matches.player4.isNull() }
                .map { it.toMatch() }
        }
    }

    override fun leaveMatch(user: User, matchId: UUID) {
        return transaction {
            val players: Players = Matches.select { Matches.id eq matchId }
                .forUpdate()
                .map {
                    Players(
                        it[Matches.player1].value,
                        it[Matches.player2]?.value,
                        it[Matches.player3]?.value,
                        it[Matches.player4]?.value
                    )
                }
                .firstOrNull() ?: throw NotFoundException()

            Matches.update({ Matches.id eq matchId }) {
                val columnToUpdate = when (user.email.value) {
                    players.player2 -> player2
                    players.player3 -> player3
                    players.player4 -> player4
                    else -> throw IllegalArgumentException("user ${user.email.value} not found in match $matchId")
                }

                it[columnToUpdate] = null
            }

        }
    }

    override fun deleteMatch(matchId: UUID): Int {
        return transaction {
            Matches.deleteWhere { Matches.id eq matchId }
        }
    }
}

data class Players(val player1: String, val player2: String?, val player3: String?, val player4: String?)

object Matches : UUIDTable("matches") {
    val player1: Column<EntityID<String>> = reference("player1", Users).index()
    val player2: Column<EntityID<String>?> = optReference("player2", Users).index()
    val player3: Column<EntityID<String>?> = optReference("player3", Users).index()
    val player4: Column<EntityID<String>?> = optReference("player4", Users).index()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val status: Column<MatchStatus> = enumerationByName("status", 20, MatchStatus::class)
    val reservation: Column<UUID?> = uuid("reservation_id").nullable()
}

class MatchesEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<MatchesEntity>(Matches)

    var player1 by UsersEntity referencedOn Matches.player1
    var player2 by UsersEntity optionalReferencedOn Matches.player2
    var player3 by UsersEntity optionalReferencedOn Matches.player3
    var player4 by UsersEntity optionalReferencedOn Matches.player4
    val createdAt by Matches.createdAt
    var status by Matches.status
    var reservation by Matches.reservation

    fun toMatch(): Match = Match(
        id.value,
        player1.toUser(),
        player2?.toUser(),
        player3?.toUser(),
        player4?.toUser(),
        createdAt,
        status,
        reservation
    )
}
