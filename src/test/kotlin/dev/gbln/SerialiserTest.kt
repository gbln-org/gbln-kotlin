/*
 * Copyright 2025 Vivian Voss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gbln

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerialiserTest {

    @Test
    fun testSerialiseInteger() {
        val result = Serialiser.toString(25.toByte())
        assertTrue(result.contains("25"))
        assertTrue(result.contains("i8"))
    }

    @Test
    fun testSerialiseString() {
        val result = Serialiser.toString("Alice")
        assertTrue(result.contains("Alice"))
        assertTrue(result.contains("s"))
    }

    @Test
    fun testSerialiseBoolean() {
        val resultTrue = Serialiser.toString(true)
        assertTrue(resultTrue.contains("true") || resultTrue.contains("t"))
        assertTrue(resultTrue.contains("b"))

        val resultFalse = Serialiser.toString(false)
        assertTrue(resultFalse.contains("false") || resultFalse.contains("f"))
    }

    @Test
    fun testSerialiseFloat() {
        val result = Serialiser.toString(19.99f)
        assertTrue(result.contains("19.99"))
        assertTrue(result.contains("f32"))
    }

    @Test
    fun testSerialiseDouble() {
        val result = Serialiser.toString(3.14159)
        assertTrue(result.contains("3.14159"))
        assertTrue(result.contains("f64"))
    }

    @Test
    fun testSerialiseNull() {
        val result = Serialiser.toString(null)
        assertTrue(result.contains("n"))
    }

    @Test
    fun testSerialiseMap() {
        val map = mapOf(
            "id" to 12345,
            "name" to "Alice"
        )

        val result = Serialiser.toString(map)
        assertTrue(result.contains("id"))
        assertTrue(result.contains("12345"))
        assertTrue(result.contains("name"))
        assertTrue(result.contains("Alice"))
        assertTrue(result.contains("{"))
        assertTrue(result.contains("}"))
    }

    @Test
    fun testSerialiseList() {
        val list = listOf("kotlin", "jvm", "android")

        val result = Serialiser.toString(list)
        assertTrue(result.contains("kotlin"))
        assertTrue(result.contains("jvm"))
        assertTrue(result.contains("android"))
        assertTrue(result.contains("["))
        assertTrue(result.contains("]"))
    }

    @Test
    fun testSerialiseNestedMap() {
        val nested = mapOf(
            "response" to mapOf(
                "status" to 200,
                "data" to mapOf(
                    "user" to mapOf(
                        "id" to 1,
                        "name" to "Alice"
                    )
                )
            )
        )

        val result = Serialiser.toString(nested)
        assertTrue(result.contains("response"))
        assertTrue(result.contains("status"))
        assertTrue(result.contains("200"))
        assertTrue(result.contains("data"))
        assertTrue(result.contains("user"))
        assertTrue(result.contains("Alice"))
    }

    @Test
    fun testSerialiseListOfMaps() {
        val list = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob")
        )

        val result = Serialiser.toString(list)
        assertTrue(result.contains("Alice"))
        assertTrue(result.contains("Bob"))
        assertTrue(result.contains("["))
        assertTrue(result.contains("]"))
    }

    @Test
    fun testSerialisePrettyString() {
        val map = mapOf(
            "id" to 123,
            "name" to "Alice"
        )

        val result = Serialiser.toPrettyString(map)
        assertTrue(result.contains("\n"))
        assertTrue(result.contains("id"))
        assertTrue(result.contains("123"))
    }

    @Test
    fun testSerialiseWithConfig() {
        val map = mapOf("name" to "Alice")

        val compactConfig = GblnConfig(prettyPrint = false)
        val compactResult = Serialiser.serialise(map, compactConfig)

        val prettyConfig = GblnConfig(prettyPrint = true)
        val prettyResult = Serialiser.serialise(map, prettyConfig)

        assertTrue(prettyResult.length > compactResult.length)
        assertTrue(prettyResult.contains("\n"))
    }

    @Test
    fun testSerialiseAllIntegerTypes() {
        assertEquals(true, Serialiser.toString(127.toByte()).contains("i8"))
        assertEquals(true, Serialiser.toString(32767.toShort()).contains("i16"))
        assertEquals(true, Serialiser.toString(2147483647).contains("i32"))
        assertEquals(true, Serialiser.toString(9223372036854775807L).contains("i64"))
    }

    @Test
    fun testSerialiseUtf8String() {
        val result = Serialiser.toString("北京")
        assertTrue(result.contains("北京"))

        val result2 = Serialiser.toString("Hello🔥")
        assertTrue(result2.contains("Hello🔥"))
    }

    @Test
    fun testSerialiseResult() {
        val result = Serialiser.toStringResult(mapOf("id" to 123))
        assertTrue(result.isSuccess())

        val gbln = (result as GblnResult.Success).value
        assertTrue(gbln.contains("123"))
    }

    @Test
    fun testSerialiseAsync() = runTest {
        val result = Serialiser.toStringAsync("Alice")
        assertTrue(result.contains("Alice"))
    }

    @Test
    fun testSerialisePrettyAsync() = runTest {
        val result = Serialiser.toPrettyStringAsync(mapOf("name" to "Alice"))
        assertTrue(result.contains("\n"))
        assertTrue(result.contains("Alice"))
    }

    @Test
    fun testExtensionToGblnString() {
        val result = mapOf("id" to 123).toGblnString()
        assertTrue(result.contains("123"))
    }

    @Test
    fun testExtensionToGblnPrettyString() {
        val result = mapOf("id" to 123).toGblnPrettyString()
        assertTrue(result.contains("\n"))
        assertTrue(result.contains("123"))
    }

    @Test
    fun testExtensionToGblnStringAsync() = runTest {
        val result = "Alice".toGblnStringAsync()
        assertTrue(result.contains("Alice"))
    }

    @Test
    fun testSerialiseComplexStructure() {
        val complex = mapOf(
            "users" to listOf(
                mapOf(
                    "id" to 1,
                    "name" to "Alice",
                    "scores" to listOf(95, 87, 92)
                ),
                mapOf(
                    "id" to 2,
                    "name" to "Bob",
                    "scores" to listOf(88, 91, 85)
                )
            ),
            "total" to 2
        )

        val result = Serialiser.toString(complex)
        assertTrue(result.contains("users"))
        assertTrue(result.contains("Alice"))
        assertTrue(result.contains("Bob"))
        assertTrue(result.contains("scores"))
        assertTrue(result.contains("95"))
        assertTrue(result.contains("total"))
    }
}
