package com.octawizard.domain.usecase

import com.octawizard.domain.usecase.club.*
import com.octawizard.domain.usecase.reservation.*
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
    bind<CreateReservation>() with provider { CreateReservation(instance(), instance(), instance()) }
    bind<CancelReservation>() with provider { CancelReservation(instance()) }
    bind<GetReservation>() with provider { GetReservation(instance()) }
    bind<GetNearestAvailableReservations>() with provider { GetNearestAvailableReservations(instance()) }

    // reservation match
    bind<UpdateMatchResult>() with provider { UpdateMatchResult(instance()) }
    bind<JoinMatch>() with provider { JoinMatch(instance()) }
    bind<LeaveMatch>() with provider { LeaveMatch(instance()) }

    // club
    bind<GetClub>() with provider { GetClub(instance()) }
    bind<CreateClub>() with provider { CreateClub(instance()) }
    bind<UpdateClubName>() with provider { UpdateClubName(instance(), instance()) }
    bind<UpdateClubAddress>() with provider { UpdateClubAddress(instance(), instance()) }
    bind<UpdateClubContacts>() with provider { UpdateClubContacts(instance()) }
    bind<GetNearestClubs>() with provider { GetNearestClubs(instance()) }
    bind<UpdateClubField>() with provider { UpdateClubField(instance(), instance()) }
    bind<AddFieldToClub>() with provider { AddFieldToClub(instance()) }
}
