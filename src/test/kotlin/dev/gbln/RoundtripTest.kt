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
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Round-trip tests: Value → GBLN → Parse → Value
 * Ensures serialisation and parsing are inverse operations.
 */
class RoundtripTest {

    @Test
    fun testRoundtripInteger() {
        val original = 25.toByte()
        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln)

        assertEquals(original, result)
    }

    @Test
    fun testRoundtripString() {
        val original = "Alice"
        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln)

        assertEquals(original, result)
    }

    @Test
    fun testRoundtripBoolean() {
        val originalTrue = true
        val gblnTrue = Serialiser.toString(originalTrue)
        val resultTrue = Parser.parse(gblnTrue)
        assertEquals(originalTrue, resultTrue)

        val originalFalse = false
        val gblnFalse = Serialiser.toString(originalFalse)
        val resultFalse = Parser.parse(gblnFalse)
        assertEquals(originalFalse, resultFalse)
    }

    @Test
    fun testRoundtripFloat() {
        val original = 19.99f
        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Float

        assertEquals(original, result, 0.001f)
    }

    @Test
    fun testRoundtripDouble() {
        val original = 3.14159265359
        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Double

        assertEquals(original, result, 0.0000000001)
    }

    @Test
    fun testRoundtripNull() {
        val original: Any? = null
        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln)

        assertEquals(original, result)
    }

    @Test
    fun testRoundtripMap() {
        val original = mapOf(
            "id" to 123,
            "name" to "Alice",
            "active" to true
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln)

        assertNotNull(result)
        assertTrue(result is Map<*, *>)

        val map = result as Map<*, *>
        assertEquals(123.toByte(), map["id"])
        assertEquals("Alice", map["name"])
        assertEquals(true, map["active"])
    }

    @Test
    fun testRoundtripList() {
        val original = listOf("kotlin", "jvm", "android")

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln)

        assertNotNull(result)
        assertTrue(result is List<*>)

        val list = result as List<*>
        assertEquals(3, list.size)
        assertEquals("kotlin", list[0])
        assertEquals("jvm", list[1])
        assertEquals("android", list[2])
    }

    @Test
    fun testRoundtripNestedMap() {
        val original = mapOf(
            "user" to mapOf(
                "id" to 1,
                "name" to "Alice",
                "settings" to mapOf(
                    "theme" to "dark",
                    "notifications" to true
                )
            )
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Map<*, *>

        val user = result["user"] as Map<*, *>
        assertEquals(1.toByte(), user["id"])
        assertEquals("Alice", user["name"])

        val settings = user["settings"] as Map<*, *>
        assertEquals("dark", settings["theme"])
        assertEquals(true, settings["notifications"])
    }

    @Test
    fun testRoundtripListOfMaps() {
        val original = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob")
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as List<*>

        assertEquals(2, result.size)

        val first = result[0] as Map<*, *>
        assertEquals(1.toByte(), first["id"])
        assertEquals("Alice", first["name"])

        val second = result[1] as Map<*, *>
        assertEquals(2.toByte(), second["id"])
        assertEquals("Bob", second["name"])
    }

    @Test
    fun testRoundtripComplexStructure() {
        val original = mapOf(
            "response" to mapOf(
                "status" to 200,
                "message" to "Success",
                "data" to listOf(
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
                "metadata" to mapOf(
                    "total" to 2,
                    "page" to 1
                )
            )
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Map<*, *>

        val response = result["response"] as Map<*, *>
        assertEquals(200.toByte(), response["status"])
        assertEquals("Success", response["message"])

        val data = response["data"] as List<*>
        assertEquals(2, data.size)

        val firstUser = data[0] as Map<*, *>
        assertEquals("Alice", firstUser["name"])

        val scores = firstUser["scores"] as List<*>
        assertEquals(3, scores.size)
        assertEquals(95.toByte(), scores[0])

        val metadata = response["metadata"] as Map<*, *>
        assertEquals(2.toByte(), metadata["total"])
    }

    @Test
    fun testRoundtripUtf8() {
        val original = mapOf(
            "city" to "北京",
            "emoji" to "Hello🔥",
            "mixed" to "Café"
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Map<*, *>

        assertEquals("北京", result["city"])
        assertEquals("Hello🔥", result["emoji"])
        assertEquals("Café", result["mixed"])
    }

    @Test
    fun testRoundtripAllIntegerTypes() {
        val original = mapOf(
            "i8" to 127.toByte(),
            "i16" to 32767.toShort(),
            "i32" to 2147483647,
            "i64" to 9223372036854775807L
        )

        val gbln = Serialiser.toString(original)
        val result = Parser.parse(gbln) as Map<*, *>

        assertEquals(127.toByte(), result["i8"])
        assertEquals(32767.toShort(), result["i16"])
        assertEquals(2147483647, result["i32"])
        assertEquals(9223372036854775807L, result["i64"])
    }

    @Test
    fun testRoundtripPrettyPrint() {
        val original = mapOf(
            "id" to 123,
            "name" to "Alice"
        )

        val prettyGbln = Serialiser.toPrettyString(original)
        val result = Parser.parse(prettyGbln) as Map<*, *>

        assertEquals(123.toByte(), result["id"])
        assertEquals("Alice", result["name"])
    }

    @Test
    fun testRoundtripAsync() = runTest {
        val original = mapOf(
            "async" to "test",
            "value" to 42
        )

        val gbln = Serialiser.toStringAsync(original)
        val result = Parser.parseAsync(gbln) as Map<*, *>

        assertEquals("test", result["async"])
        assertEquals(42.toByte(), result["value"])
    }

    @Test
    fun testRoundtripFile() {
        val tempFile = Files.createTempFile("gbln_roundtrip", ".gbln").toFile()
        tempFile.deleteOnExit()

        val original = mapOf(
            "users" to listOf(
                mapOf("id" to 1, "name" to "Alice"),
                mapOf("id" to 2, "name" to "Bob")
            ),
            "total" to 2
        )

        Io.writeFile(tempFile.absolutePath, original)
        val result = Io.readFile(tempFile.absolutePath) as Map<*, *>

        assertEquals(2.toByte(), result["total"])

        val users = result["users"] as List<*>
        assertEquals(2, users.size)

        val firstUser = users[0] as Map<*, *>
        assertEquals("Alice", firstUser["name"])
    }

    @Test
    fun testRoundtripFileAsync() = runTest {
        val tempFile = Files.createTempFile("gbln_roundtrip_async", ".gbln").toFile()
        tempFile.deleteOnExit()

        val original = mapOf(
            "test" to "async",
            "count" to 5
        )

        Io.writeFileAsync(tempFile.absolutePath, original)
        val result = Io.readFileAsync(tempFile.absolutePath) as Map<*, *>

        assertEquals("async", result["test"])
        assertEquals(5.toByte(), result["count"])
    }

    @Test
    fun testRoundtripMultipleTimes() {
        var current: Any? = mapOf("value" to 123)

        // Do 5 roundtrips
        repeat(5) {
            val gbln = Serialiser.toString(current)
            current = Parser.parse(gbln)
        }

        val final = current as Map<*, *>
        assertEquals(123.toByte(), final["value"])
    }

    @Test
    fun testRoundtripExtensionFunctions() {
        val original = mapOf("test" to "extensions")

        val gbln = original.toGblnString()
        val result = gbln.parseGbln() as Map<*, *>

        assertEquals("extensions", result["test"])
    }

    @Test
    fun testRoundtripExtensionFunctionsAsync() = runTest {
        val original = mapOf("async" to "extensions")

        val gbln = original.toGblnStringAsync()
        val result = gbln.parseGblnAsync() as Map<*, *>

        assertEquals("extensions", result["async"])
    }
}
