package com.octawizard.controller

import com.octawizard.controller.club.ClubController
import com.octawizard.controller.reservation.ReservationController
import com.octawizard.controller.user.UserController
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

val controllerModule = DI.Module("controller") {
    // user
    bind<UserController>() with provider { UserController(instance(), instance(), instance(), instance()) }
    // club
    bind<ClubController>() with provider {
        ClubController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
        )
    }
    // reservation
    bind<ReservationController>() with provider {
        ReservationController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
        )
    }
}
