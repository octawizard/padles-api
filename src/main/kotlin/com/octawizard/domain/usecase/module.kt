package com.octawizard.domain.usecase

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
    bind<CancelReservation>() with provider { CancelReservation(instance(), instance()) }
    bind<GetReservation>() with provider { GetReservation(instance()) }
    bind<GetNearestAvailableReservations>() with provider { GetNearestAvailableReservations(instance()) }

    // reservation match
    bind<UpdateMatchResult>() with provider { UpdateMatchResult(instance()) }
    bind<JoinMatch>() with provider { JoinMatch(instance(), instance()) }
    bind<LeaveMatch>() with provider { LeaveMatch(instance(), instance()) }

    // club
    bind<GetClub>() with provider { GetClub(instance()) }
    bind<CreateClub>() with provider { CreateClub(instance()) }
    bind<UpdateClubName>() with provider { UpdateClubName(instance(), instance()) }
    bind<UpdateClubAddress>() with provider { UpdateClubAddress(instance(), instance()) }
    bind<UpdateClubAvgPrice>() with provider { UpdateClubAvgPrice(instance()) }
    bind<UpdateClubContacts>() with provider { UpdateClubContacts(instance()) }
    bind<GetNearestClubs>() with provider { GetNearestClubs(instance()) }
    bind<UpdateClubField>() with provider { UpdateClubField(instance(), instance()) }
    bind<AddFieldToClub>() with provider { AddFieldToClub(instance()) }
    bind<UpdateClubAvailability>() with provider { UpdateClubAvailability(instance()) }
    bind<SearchClubsByName>() with provider { SearchClubsByName(instance()) }
}
