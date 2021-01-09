package com.octawizard.domain.usecase

import com.octawizard.domain.usecase.club.*
import com.octawizard.domain.usecase.reservation.*
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.transaction.TransactionRepository
import com.octawizard.repository.user.UserRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {
    private val fakeRepoModule = DI.Module("repository") {
        bind<ClubRepository>() with singleton { mockk(relaxed = true) }
        bind<ReservationRepository>() with singleton { mockk(relaxed = true) }
        bind<TransactionRepository>() with singleton { mockk(relaxed = true) }
        bind<UserRepository>() with singleton { mockk(relaxed = true) }
    }

    @Test
    fun `UseCaseModule should inject dependencies for use cases`() {
        val kodein = DI {
            import(fakeRepoModule)
            import(useCaseModule)
        }

        // user
        assertNotNull(kodein.direct.instance<CreateUser>())
        assertNotNull(kodein.direct.instance<DeleteUser>())
        assertNotNull(kodein.direct.instance<GetUser>())
        assertNotNull(kodein.direct.instance<UpdateUser>())

        // reservation
        assertNotNull(kodein.direct.instance<CancelReservation>())
        assertNotNull(kodein.direct.instance<CreateReservation>())
        assertNotNull(kodein.direct.instance<GetNearestAvailableReservations>())
        assertNotNull(kodein.direct.instance<GetReservation>())

        // reservation match
        assertNotNull(kodein.direct.instance<JoinMatch>())
        assertNotNull(kodein.direct.instance<LeaveMatch>())
        assertNotNull(kodein.direct.instance<UpdateMatchResult>())

        // club
        assertNotNull(kodein.direct.instance<AddFieldToClub>())
        assertNotNull(kodein.direct.instance<CreateClub>())
        assertNotNull(kodein.direct.instance<GetClub>())
        assertNotNull(kodein.direct.instance<GetNearestClubs>())
        assertNotNull(kodein.direct.instance<UpdateClubAddress>())
        assertNotNull(kodein.direct.instance<UpdateClubAvailability>())
        assertNotNull(kodein.direct.instance<UpdateClubAvgPrice>())
        assertNotNull(kodein.direct.instance<UpdateClubContacts>())
        assertNotNull(kodein.direct.instance<UpdateClubField>())
        assertNotNull(kodein.direct.instance<UpdateClubName>())
        assertNotNull(kodein.direct.instance<SearchClubsByName>())
    }

}
