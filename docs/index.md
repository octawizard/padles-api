# Padles API
This is a Kotlin project that exposes a REST API for Padel match reservations.

Mainly is based on the following frameworks/libraries:
* [Ktor](https://ktor.io/)
* [Kodein](https://github.com/Kodein-Framework/Kodein-DI)
* [Exposed](https://github.com/JetBrains/Exposed) (with Postgres driver)
* [Redisson](https://github.com/redisson/redisson)
* [KMongo](https://litote.org/kmongo/)

## How to run
* Start the docker containers:
```
docker-compose up
```

* Run the main function in `com.octawizard.server.server.kt`

* API will be reachable at [`http://localhost:1111`](http://localhost:1111). API Calls need to have a JWT Token as
 auth http header.

## Use cases
* Users
    * create a user
    * get a user
    * update a user
    * delete a user

* Reservations
    * make a reservation
    * update a reservation (only by reservation owner)
        * cancel a reservation
        * ~~pay a reservation~~

    * add/update result of the reserved match
    * patch a reservation match (authorized players)
        * join a reservation
        * leave a reservation

    * search for incomplete reservation (missing players)

* Club
    * register a club
    * update a club
    * get a club
    * add/update field availability
    * search for available fields (close to the user)
    * search for near clubs (close to the user)
    * search for clubs by name

#### TODO 
* get all matches of a user
* get all reservation of a user

* review a user
* review a club

* mark club as favourite
* mark user as favourite
