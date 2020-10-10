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