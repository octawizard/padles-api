package com.octawizard.repository

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

annotation class IgnoreTooGenericExceptionCaught

@IgnoreTooGenericExceptionCaught
suspend fun retry(times: Int = 3, block: suspend () -> Unit) {
    require(times > 0)
    (1..times).forEach { i ->
        try {
            return block()
        } catch (e: Exception) {
            logger.warn(e) { "Try attempt $i failed" }
            /* retry */
            if (i >= times) {
                throw e
            }
        }
    }
    // ignore add in cache, todo send some metric to track it
}
