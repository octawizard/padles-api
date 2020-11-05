package com.octawizard.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailTest {

    @Test
    fun `Email should validate its value`() {
        val value = "valid@test.com"
        val email = Email(value)
        assertEquals(value, email.value)
    }

    @Test
    fun `Email should not validate empty value`() {
        assertThrows(IllegalStateException::class.java) { Email("") }
    }

    @Test
    fun `Email should not validate invalid value`() {
        assertThrows(IllegalStateException::class.java) { Email("not an email") }
    }

    @Test
    fun `Email should not be equals to another object`() {
        assertFalse(Email("test@test.com").equals(1))
    }
}
