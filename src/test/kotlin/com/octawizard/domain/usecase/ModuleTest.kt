package com.octawizard.domain.usecase

import com.octawizard.domain.usecase.match.CreateMatch
import com.octawizard.domain.usecase.match.DeleteMatch
import com.octawizard.domain.usecase.match.FindAvailableMatches
import com.octawizard.domain.usecase.match.GetMatch
import com.octawizard.domain.usecase.reservation.JoinMatch
import com.octawizard.domain.usecase.reservation.LeaveMatch
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.UserRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {
    private val fakeRepoModule = DI.Module("repository") {
        bind<MatchRepository>() with singleton { mockk(relaxed = true) }
        bind<UserRepository>() with singleton { mockk(relaxed = true) }
    }

    @Test
    fun `UseCaseModule should inject dependencies for use cases`() {
        val kodein = DI {
            import(fakeRepoModule)
            import(useCaseModule)
        }

        assertNotNull(kodein.direct.instance<CreateMatch>())
        assertNotNull(kodein.direct.instance<CreateUser>())
        assertNotNull(kodein.direct.instance<DeleteMatch>())
        assertNotNull(kodein.direct.instance<FindAvailableMatches>())
        assertNotNull(kodein.direct.instance<GetMatch>())
        assertNotNull(kodein.direct.instance<GetUser>())
        assertNotNull(kodein.direct.instance<JoinMatch>())
        assertNotNull(kodein.direct.instance<LeaveMatch>())
        assertNotNull(kodein.direct.instance<UpdateUser>())
    }

}
