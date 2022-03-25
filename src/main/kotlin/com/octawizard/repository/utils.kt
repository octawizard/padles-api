package com.octawizard.repository

suspend fun retry(times: Int = 3, block: suspend () -> Unit) {
    require(times > 0)
    (1..times).forEach { i ->
        try {
            return block()
        } catch (e: Exception) { /* retry */ }
    }
    // ignore add in cache, todo send some metric to track it
}
