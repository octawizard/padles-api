package com.octawizard.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilsTest {

    @Test
    fun `retry function should retry executing block default times at most when block throws exceptions`() {
        val block = mockk<suspend () -> Unit>()
        coEvery { block() } throws RuntimeException("test retry")

        runBlocking { retry { block() } }

        coVerify(exactly = 3) { block() }
    }

    @Test
    fun `retry function should retry executing block given times at most when block throws exceptions`() {
        val block = mockk<suspend () -> Unit>()
        coEvery { block() } throws RuntimeException("test retry")

        runBlocking { retry(1) { block() } }

        coVerify(exactly = 1) { block() }
    }

    @Test
    fun `retry function should retry executing block until block invocation isn't throwing exception`() {
        val block = mockk<suspend () -> Unit>()
        coEvery { block() } throws RuntimeException("test retry") andThen Unit

        runBlocking { retry(5) { block() } }

        coVerify(exactly = 2) { block() }
    }

    @Test
    fun `retry function should throw exception when providing retry times is below 1`() {
        val block = mockk<suspend () -> Unit>()

        assertThrows(IllegalArgumentException::class.java) { runBlocking { retry(0) { block() } } }
    }
}
