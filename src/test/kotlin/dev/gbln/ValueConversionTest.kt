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

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ValueConversionTest {

    @Test
    fun testConvertNull() {
        val managed = ValueConversion.toGbln(null)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsNull(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(null, back)
        }
    }

    @Test
    fun testConvertBoolean() {
        val managedTrue = ValueConversion.toGbln(true)
        managedTrue.use { m ->
            assertTrue(FfiWrapper.valueIsBool(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(true, back)
        }

        val managedFalse = ValueConversion.toGbln(false)
        managedFalse.use { m ->
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(false, back)
        }
    }

    @Test
    fun testConvertByte() {
        val managed = ValueConversion.toGbln(127.toByte())
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsI8(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(127.toByte(), back)
        }
    }

    @Test
    fun testConvertShort() {
        val managed = ValueConversion.toGbln(32767.toShort())
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsI16(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(32767.toShort(), back)
        }
    }

    @Test
    fun testConvertInt() {
        val managed = ValueConversion.toGbln(2147483647)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsI32(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(2147483647, back)
        }
    }

    @Test
    fun testConvertLong() {
        val managed = ValueConversion.toGbln(9223372036854775807L)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsI64(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(9223372036854775807L, back)
        }
    }

    @Test
    fun testConvertFloat() {
        val managed = ValueConversion.toGbln(19.99f)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsF32(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(19.99f, back as Float, 0.001f)
        }
    }

    @Test
    fun testConvertDouble() {
        val managed = ValueConversion.toGbln(3.14159265359)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsF64(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals(3.14159265359, back as Double, 0.0000000001)
        }
    }

    @Test
    fun testConvertString() {
        val managed = ValueConversion.toGbln("Alice")
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsString(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals("Alice", back)
        }
    }

    @Test
    fun testConvertUtf8String() {
        val managed = ValueConversion.toGbln("北京")
        managed.use { m ->
            val back = ValueConversion.fromGbln(m.pointer())
            assertEquals("北京", back)
        }
    }

    @Test
    fun testConvertMap() {
        val original = mapOf(
            "id" to 123,
            "name" to "Alice",
            "active" to true
        )

        val managed = ValueConversion.toGbln(original)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsObject(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertNotNull(back)
            assertTrue(back is Map<*, *>)

            val map = back as Map<*, *>
            assertEquals(123.toByte(), map["id"])
            assertEquals("Alice", map["name"])
            assertEquals(true, map["active"])
        }
    }

    @Test
    fun testConvertList() {
        val original = listOf("kotlin", "jvm", "android")

        val managed = ValueConversion.toGbln(original)
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsArray(m.pointer()))
            val back = ValueConversion.fromGbln(m.pointer())
            assertNotNull(back)
            assertTrue(back is List<*>)

            val list = back as List<*>
            assertEquals(3, list.size)
            assertEquals("kotlin", list[0])
            assertEquals("jvm", list[1])
            assertEquals("android", list[2])
        }
    }

    @Test
    fun testConvertNestedMap() {
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

        val managed = ValueConversion.toGbln(original)
        managed.use { m ->
            val back = ValueConversion.fromGbln(m.pointer()) as Map<*, *>
            val user = back["user"] as Map<*, *>
            val settings = user["settings"] as Map<*, *>

            assertEquals("dark", settings["theme"])
            assertEquals(true, settings["notifications"])
        }
    }

    @Test
    fun testConvertListOfMaps() {
        val original = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob")
        )

        val managed = ValueConversion.toGbln(original)
        managed.use { m ->
            val back = ValueConversion.fromGbln(m.pointer()) as List<*>

            val first = back[0] as Map<*, *>
            assertEquals(1.toByte(), first["id"])
            assertEquals("Alice", first["name"])

            val second = back[1] as Map<*, *>
            assertEquals(2.toByte(), second["id"])
            assertEquals("Bob", second["name"])
        }
    }

    @Test
    fun testAutoSelectIntegerType() {
        // Should select i8 for small values
        val small = ValueConversion.toGbln(25)
        small.use { m ->
            assertTrue(FfiWrapper.valueIsI8(m.pointer()))
        }

        // Should select i16 for medium values
        val medium = ValueConversion.toGbln(1000)
        medium.use { m ->
            assertTrue(FfiWrapper.valueIsI16(m.pointer()))
        }

        // Should select i32 for large values
        val large = ValueConversion.toGbln(100000)
        large.use { m ->
            assertTrue(FfiWrapper.valueIsI32(m.pointer()))
        }
    }

    @Test
    fun testAutoSelectStringType() {
        // Should select appropriate string type based on length
        val short = ValueConversion.toGbln("Hi")
        short.use { m ->
            assertTrue(FfiWrapper.valueIsString(m.pointer()))
        }

        val medium = ValueConversion.toGbln("Hello World")
        medium.use { m ->
            assertTrue(FfiWrapper.valueIsString(m.pointer()))
        }

        val long = ValueConversion.toGbln("A".repeat(100))
        long.use { m ->
            assertTrue(FfiWrapper.valueIsString(m.pointer()))
        }
    }

    @Test
    fun testExtensionToGbln() {
        val managed = "Alice".toGbln()
        managed.use { m ->
            assertTrue(FfiWrapper.valueIsString(m.pointer()))
        }
    }

    @Test
    fun testExtensionFromGbln() {
        val managed = ValueConversion.toGbln("Alice")
        managed.use { m ->
            val back = m.pointer().fromGbln()
            assertEquals("Alice", back)
        }
    }

    @Test
    fun testConvertComplexStructure() {
        val original = mapOf(
            "response" to mapOf(
                "status" to 200,
                "data" to listOf(
                    mapOf("id" to 1, "name" to "Alice", "scores" to listOf(95, 87, 92)),
                    mapOf("id" to 2, "name" to "Bob", "scores" to listOf(88, 91, 85))
                ),
                "metadata" to mapOf(
                    "total" to 2,
                    "timestamp" to 1234567890L
                )
            )
        )

        val managed = ValueConversion.toGbln(original)
        managed.use { m ->
            val back = ValueConversion.fromGbln(m.pointer()) as Map<*, *>
            val response = back["response"] as Map<*, *>
            val data = response["data"] as List<*>
            val metadata = response["metadata"] as Map<*, *>

            assertEquals(200.toByte(), response["status"])
            assertEquals(2, data.size)
            assertEquals(2.toByte(), metadata["total"])

            val firstUser = data[0] as Map<*, *>
            assertEquals("Alice", firstUser["name"])

            val scores = firstUser["scores"] as List<*>
            assertEquals(3, scores.size)
        }
    }
}
