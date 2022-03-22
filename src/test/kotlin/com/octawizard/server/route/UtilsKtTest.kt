package com.octawizard.server.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class UtilsKtTest {

    @Nested
    inner class GetQueryParamOrDefaultTest {
        @Test
        fun `get Query Param String`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            val expected = "10"
            parametersBuilder["key"] = expected
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val result = call.getQueryParamOrDefault("key", "default")
            assertEquals(expected, result)
        }

        @Test
        fun `get Query Param missing`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val expected = 1
            val result = call.getQueryParamOrDefault("key", expected)
            assertEquals(expected, result)
        }

        @Test
        fun `get Query Param Int`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            val expected = "10"
            parametersBuilder["key"] = expected
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Int>("key", 1)
            assertEquals(expected.toInt(), result)
        }

        @Test
        fun `get Query Param Int with null default`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Int>("key")
            assertEquals(null, result)
        }

        @Test
        fun `get Query Param Double`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            val expected = "10.10"
            parametersBuilder["key"] = expected
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Double>("key", 1.1)
            assertEquals(expected.toDouble(), result)
        }

        @Test
        fun `get Query Param Double with null default`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Double>("key")
            assertEquals(null, result)
        }

        @Test
        fun `get Query Param Long`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            val expected = "10"
            parametersBuilder["key"] = expected
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Long>("key", 1L)
            assertEquals(expected.toLong(), result)
        }

        @Test
        fun `get Query Param Long with null default`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<Long>("key")
            assertEquals(null, result)
        }

        @Test
        fun `get Query Param LocalDate`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            val expected = LocalDate.now()
            parametersBuilder["key"] = expected.format(DateTimeFormatter.ISO_DATE)
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val default = LocalDate.now().plusDays(1)
            val result = call.getQueryParamOrDefault<LocalDate>("key", default, DateTimeFormatter.ISO_DATE)
            assertEquals(expected, result)
        }

        @Test
        fun `get Query Param LocalDate with null default`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val result = call.getQueryParamOrDefault<LocalDate>("key", dateTimeFormatter = DateTimeFormatter.ISO_DATE)
            assertEquals(null, result)
        }

        @Test
        fun `get Query Param LocalDate malformed - default`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            val parametersBuilder = ParametersBuilder(size = 1)
            parametersBuilder["key"] =  "not a date"
            val params = parametersBuilder.build()
            every { request.queryParameters } returns params
            every { call.request } returns request
            val default = LocalDate.now().plusDays(1)
            val result = call.getQueryParamOrDefault<LocalDate>("key", default, DateTimeFormatter.ISO_DATE)
            assertEquals(default, result)
        }

        @Test
        fun `get Query Param LocalDate missing`() {
            val call = mockk<ApplicationCall>()
            val request = mockk<ApplicationRequest>()
            every { request.queryParameters } returns Parameters.Empty
            every { call.request } returns request
            val default = LocalDate.now().plusDays(1)
            val result = call.getQueryParamOrDefault<LocalDate>("key", default, DateTimeFormatter.ISO_DATE)
            assertEquals(default, result)
        }
    }
}
