package com.octawizard.domain.model

import java.lang.IllegalArgumentException

class Email(val value: String) {

    init {
        if (!value.isValidEmail()) {
            throw IllegalArgumentException("not a valid email: $value")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Email

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Email(value='$value')"
    }


}

private val Prefix = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}"
private val Suffix = "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
private val EmailRegex = "$Prefix@$Suffix".toRegex()

fun String.isValidEmail(): Boolean {
    return !this.isBlank() && EmailRegex.matches(this)
}
