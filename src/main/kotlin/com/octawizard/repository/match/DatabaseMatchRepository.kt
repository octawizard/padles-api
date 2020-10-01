package com.octawizard.repository.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.user.Users
import com.octawizard.repository.user.UsersEntity
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
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

    override fun joinMatch(user: User, match: Match): Match {
        TODO("Not yet implemented")
    }

    override fun allAvailableMatches(): List<Match> {
        return transaction {
            MatchesEntity.find { Matches.player2.isNull() or Matches.player3.isNull() or Matches.player4.isNull() }
                .map { it.toMatch() }
        }
    }
}

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

    val player1 by UsersEntity referencedOn Matches.player1
    val player2 by UsersEntity optionalReferencedOn Matches.player2
    val player3 by UsersEntity optionalReferencedOn Matches.player3
    val player4 by UsersEntity optionalReferencedOn Matches.player4
    val createdAt by Matches.createdAt
    val status by Matches.status
    val reservation by Matches.reservation

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
