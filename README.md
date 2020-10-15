# Padles API [![Build Status](https://travis-ci.org/octawizard/padles-api.svg?branch=master)](https://travis-ci.org/octawizard/padles-api) [![codecov](https://codecov.io/gh/octawizard/padles-api/branch/master/graph/badge.svg)](https://codecov.io/gh/octawizard/padles-api)


Small REST API project using 
* Ktor
* Kodein
* Exposed (with Postgres driver)
* Redisson

## How to run
* Start the docker containers (Postgres and Redis):
`docker-compose up`

* Run the main function in `com.octawizard.server.server.kt`

## Use cases
* create a user (done)
* get a user (done)
* update a user (done)
* delete a user (done)

* make a reservation
* join a reservation
* leave a reservation
* pay a reservation
* add result of the reserved match

* search for available fields (close to user, filters, ...)
* search for incomplete reservation (missing players)

* register a club
* update a club
* get a club
* add/update field availability

* get all matches of a user
* get all reservation of a user

* review a user
* review a club

* mark club as favourite
* mark user as favourite
