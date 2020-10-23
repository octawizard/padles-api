package com.octawizard.domain.usecase

import com.octawizard.domain.usecase.match.*
import com.octawizard.domain.usecase.reservation.CancelReservation
import com.octawizard.domain.usecase.reservation.CreateReservation
import com.octawizard.domain.usecase.reservation.GetReservation
import com.octawizard.domain.usecase.reservation.JoinMatch
import com.octawizard.domain.usecase.reservation.LeaveMatch
import com.octawizard.domain.usecase.reservation.UpdateMatchResult
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider


val useCaseModule = DI.Module("useCase") {
    // user
    bind<CreateUser>() with provider { CreateUser(instance()) }
    bind<GetUser>() with provider { GetUser(instance()) }
    bind<UpdateUser>() with provider { UpdateUser(instance()) }
    bind<DeleteUser>() with provider { DeleteUser(instance()) }

    // reservation
    bind<CreateReservation>() with provider { CreateReservation(instance(), instance()) }
    bind<CancelReservation>() with provider { CancelReservation(instance()) }
    bind<GetReservation>() with provider { GetReservation(instance()) }

    // reservation match
    bind<UpdateMatchResult>() with provider { UpdateMatchResult(instance()) }
    bind<JoinMatch>() with provider { JoinMatch(instance()) }
    bind<LeaveMatch>() with provider { LeaveMatch(instance()) }

}
