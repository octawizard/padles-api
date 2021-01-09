package com.octawizard.controller

import com.octawizard.controller.club.ClubController
import com.octawizard.controller.reservation.ReservationController
import com.octawizard.controller.user.UserController
import com.octawizard.domain.usecase.club.AddFieldToClub
import com.octawizard.domain.usecase.club.CreateClub
import com.octawizard.domain.usecase.club.GetClub
import com.octawizard.domain.usecase.club.GetNearestClubs
import com.octawizard.domain.usecase.club.SearchClubsByName
import com.octawizard.domain.usecase.club.UpdateClubAddress
import com.octawizard.domain.usecase.club.UpdateClubAvailability
import com.octawizard.domain.usecase.club.UpdateClubAvgPrice
import com.octawizard.domain.usecase.club.UpdateClubContacts
import com.octawizard.domain.usecase.club.UpdateClubField
import com.octawizard.domain.usecase.club.UpdateClubName
import com.octawizard.domain.usecase.reservation.CancelReservation
import com.octawizard.domain.usecase.reservation.CreateReservation
import com.octawizard.domain.usecase.reservation.GetNearestAvailableReservations
import com.octawizard.domain.usecase.reservation.GetReservation
import com.octawizard.domain.usecase.reservation.JoinMatch
import com.octawizard.domain.usecase.reservation.LeaveMatch
import com.octawizard.domain.usecase.reservation.UpdateMatchResult
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {
    private val fakeUseCaseModule = DI.Module("usecase") {
        bind<AddFieldToClub>() with singleton { mockk(relaxed = true) }
        bind<CreateClub>() with singleton { mockk(relaxed = true) }
        bind<GetClub>() with singleton { mockk(relaxed = true) }
        bind<GetNearestClubs>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubAddress>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubAvgPrice>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubAvailability>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubContacts>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubField>() with singleton { mockk(relaxed = true) }
        bind<UpdateClubName>() with singleton { mockk(relaxed = true) }
        bind<SearchClubsByName>() with singleton { mockk(relaxed = true) }

        bind<CancelReservation>() with singleton { mockk(relaxed = true) }
        bind<CreateReservation>() with singleton { mockk(relaxed = true) }
        bind<GetNearestAvailableReservations>() with singleton { mockk(relaxed = true) }
        bind<GetReservation>() with singleton { mockk(relaxed = true) }
        bind<JoinMatch>() with singleton { mockk(relaxed = true) }
        bind<LeaveMatch>() with singleton { mockk(relaxed = true) }
        bind<UpdateMatchResult>() with singleton { mockk(relaxed = true) }

        bind<CreateUser>() with singleton { mockk(relaxed = true) }
        bind<DeleteUser>() with singleton { mockk(relaxed = true) }
        bind<GetUser>() with singleton { mockk(relaxed = true) }
        bind<UpdateUser>() with singleton { mockk(relaxed = true) }
    }

    @Test
    fun `ControllerModule should inject dependencies for controller`() {
        val kodein = DI {
            import(fakeUseCaseModule)
            import(controllerModule)
        }

        Assertions.assertNotNull(kodein.direct.instance<ClubController>())
        Assertions.assertNotNull(kodein.direct.instance<ReservationController>())
        Assertions.assertNotNull(kodein.direct.instance<UserController>())
    }

}
