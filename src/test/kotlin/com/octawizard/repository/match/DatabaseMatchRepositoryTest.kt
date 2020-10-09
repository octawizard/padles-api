package com.octawizard.repository.match

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.user.Users
import io.ktor.features.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseMatchRepositoryTest {
    private lateinit var repository: DatabaseMatchRepository

    private val user1 = User(Email("user1@mail.com"), "player1", Gender.male)
    private val user2 = User(Email("user2@mail.com"), "player2", Gender.female)
    private val user3 = User(Email("user3@email.com"), "player3", Gender.other)
    private val user4 = User(Email("user4@email.com"), "player4", Gender.male)

    @BeforeAll
    fun `init database`() {
        Database.connect(
                url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                user = "test",
                password = "")


        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users, Matches)
        }
        repository = DatabaseMatchRepository()
    }

    @AfterEach
    fun `clean database`() {
        transaction {
            Matches.deleteAll()
            Users.deleteAll()
        }
    }

    @Test
    fun `DatabaseMatchRepository should create a match and return it`() {
        addUsersToDb(user1, user2)

        val (_, player1, player2, player3, player4, _, status, _) =
                repository.createMatch(user1, user2, null, null, MatchStatus.Draft)

        assertEquals(user1, player1)
        assertEquals(user2, player2)
        assertNull(player3)
        assertNull(player4)
        assertEquals(MatchStatus.Draft, status)
    }

    @Test
    fun `DatabaseMatchRepository should return a match given the id if it exists`() {
        val matchId = createMatch(user1, user2)

        val (id, player1, player2, player3, player4, createdAt, status, reservationId) =
                repository.getMatch(matchId)!!

        assertEquals(matchId, id)
        assertEquals(user1, player1)
        assertEquals(user2, player2)
        assertNull(player3)
        assertNull(player4)
        assertEquals(MatchStatus.Draft, status)
    }

    @Test
    fun `DatabaseMatchRepository should return null given the id if a match doesn't exist`() {
        val match = repository.getMatch(UUID.randomUUID())
        assertNull(match)
    }

    @Test
    fun `DatabaseMatchRepository should join a user in a match if there is an available spot`() {
        addUsersToDb(user3)
        val matchId = createMatch(user1, user2)

        repository.joinMatch(user3, matchId)

        val (_, _, _, player3, _, _, _, _) = repository.getMatch(matchId)!!
        assertEquals(user3, player3)
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when trying to join a user in a match that doesn't exist`() {
        assertThrows(NotFoundException::class.java) { repository.joinMatch(user3, UUID.randomUUID()) }
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when trying to join a user in a match that doesn't have available spot`() {
        val additionalUser = User(Email("additional@email.com"), "player5")

        val matchId = createMatch(user1, user2, user3, user4)

        assertThrows(IllegalStateException::class.java) { repository.joinMatch(additionalUser, matchId) }
    }

    @Test
    fun `DatabaseMatchRepository should return all the matches that have available spots`() {
        createMatch(user1, user2, user3, user4) // not available match
        val availableMatchIds = listOf(createMatch(user1), createMatch(user1, user2))

        val matches = repository.allAvailableMatches()

        assertEquals(availableMatchIds, matches.map { it.id })
    }

    @Test
    fun `DatabaseMatchRepository should remove a user from a match where he joined`() {
        val matchId = createMatch(user1, user2)

        repository.leaveMatch(user2, matchId)

        val (_, player1, player2, player3, player4, _, _, _) = repository.getMatch(matchId)!!
        assertFalse(listOfNotNull(player1, player2, player3, player4).contains(user2))
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when removing a user from a match that doesn't exist`() {
        assertThrows(NotFoundException::class.java) { repository.leaveMatch(user3, UUID.randomUUID()) }
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when removing a user from a match where he hasn't join`() {
        val matchId = createMatch(user1, user2)

        assertThrows(IllegalArgumentException::class.java) { repository.leaveMatch(user3, matchId) }
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when removing the match creator user`() {
        val matchId = createMatch(user1, user2)

        assertThrows(IllegalArgumentException::class.java) { repository.leaveMatch(user1, matchId) }
    }

    @Test
    fun `DatabaseMatchRepository should delete a match`() {
        val matchId = createMatch(user1, user2)

        repository.deleteMatch(matchId)

        assertNull(repository.getMatch(matchId))
    }

    @Test
    fun `DatabaseMatchRepository should throw exception when deleting a match that doesn't exist`() {
        assertThrows(NotFoundException::class.java) { repository.deleteMatch(UUID.randomUUID()) }
    }

    private fun addUsersToDb(vararg users: User) = addUsersToDb(users.asList())

    private fun addUsersToDb(users: List<User>) {
        try {
            transaction {
                Users.batchInsert(users) { user: User ->
                    this[Users.id] = EntityID(user.email.value, Users)
                    this[Users.name] = user.name
                    this[Users.gender] = user.gender
                    this[Users.createdAt] = user.createdAt
                }
            }
        } catch (e: ExposedSQLException) {
            // users already inserted in database
        }
    }

    private fun createMatch(player1: User, player2: User? = null, player3: User? = null, player4: User? = null): UUID {
        val users = listOfNotNull(player1, player2, player3, player4)
        addUsersToDb(users)
        return transaction {
            Matches.insertAndGetId {
                it[Matches.player1] = EntityID(player1.email.value, Users)
                it[Matches.player2] = player2?.let { u -> EntityID(u.email.value, Users) }
                it[Matches.player3] = player3?.let { u -> EntityID(u.email.value, Users) }
                it[Matches.player4] = player4?.let { u -> EntityID(u.email.value, Users) }
                it[Matches.createdAt] = LocalDateTime.now()
                it[Matches.status] = MatchStatus.Draft
                it[Matches.reservation] = null
            }
        }.value
    }
}
