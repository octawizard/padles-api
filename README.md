# Padles API [![Build Status](https://travis-ci.com/octawizard/padles-api.svg?branch=master)](https://travis-ci.org/octawizard/padles-api) [![codecov](https://codecov.io/gh/octawizard/padles-api/branch/master/graph/badge.svg)](https://codecov.io/gh/octawizard/padles-api)

This is a Kotlin project that exposes a REST API for Padel match reservations.

Mainly is based on the following frameworks/libraries:
* [Ktor](https://ktor.io/)
* [Kodein](https://github.com/Kodein-Framework/Kodein-DI)
* [Exposed](https://github.com/JetBrains/Exposed) (with Postgres driver)
* [Redisson](https://github.com/redisson/redisson)
* [KMongo](https://litote.org/kmongo/)

## How to run
* Start the docker containers (Postgres and Redis):
`docker-compose up`

* Run the main function in `com.octawizard.server.server.kt`

* API will be reachable at `http://localhost:1111`

## Use cases
* Users
- \[x] create a user
- \[x] get a user
- \[x] update a user
- \[x] delete a user

* Reservations
- \[x] make a reservation
- \[x] update a reservation (only by reservation owner)
    - \[x] cancel a reservation
    - \[ ] pay a reservation

- \[x] add/update result of the reserved match
- \[x] patch a reservation match (authorized players)
    * join a reservation
    * leave a reservation

- \[x] search for incomplete reservation (missing players)

* Club
- \[x] register a club
- \[x] update a club
- \[x] get a club
- \[x] add/update field availability
- \[x] search for available fields (close to user, filters, ...)
- \[x] search for near clubs (close to user, filters, ...)

#### TODO 
* get all matches of a user
* get all reservation of a user

* review a user
* review a club

* mark club as favourite
* mark user as favourite
