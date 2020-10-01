package com.octawizard.domain.usecase

import com.octawizard.domain.usecase.match.*
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider


val useCaseModule = DI.Module("useCase") {
    bind<CreateMatch>() with provider { CreateMatch(instance(), instance()) }
    bind<CreateUser>() with provider { CreateUser(instance()) }
    bind<DeleteMatch>() with provider { DeleteMatch(instance()) }
    bind<FindAvailableMatches>() with provider { FindAvailableMatches(instance()) }
    bind<GetMatch>() with provider { GetMatch(instance()) }
    bind<GetUser>() with provider { GetUser(instance()) }
    bind<JoinMatch>() with provider { JoinMatch(instance()) }
    bind<LeaveMatch>() with provider { LeaveMatch(instance()) }
    bind<UpdateUser>() with provider { UpdateUser(instance()) }
}
