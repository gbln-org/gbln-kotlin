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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun testParseSimpleInteger() {
        val result = Parser.parse("age<i8>(25)")
        assertNotNull(result)
        assertEquals(25.toByte(), result)
    }

    @Test
    fun testParseSimpleString() {
        val result = Parser.parse("name<s32>(Alice)")
        assertNotNull(result)
        assertEquals("Alice", result)
    }

    @Test
    fun testParseBoolean() {
        val resultTrue = Parser.parse("active<b>(t)")
        assertEquals(true, resultTrue)

        val resultFalse = Parser.parse("active<b>(false)")
        assertEquals(false, resultFalse)
    }

    @Test
    fun testParseFloat() {
        val result = Parser.parse("price<f32>(19.99)")
        assertNotNull(result)
        assertTrue(result is Float)
        assertEquals(19.99f, result as Float, 0.001f)
    }

    @Test
    fun testParseDouble() {
        val result = Parser.parse("pi<f64>(3.14159265359)")
        assertNotNull(result)
        assertTrue(result is Double)
        assertEquals(3.14159265359, result as Double, 0.0000000001)
    }

    @Test
    fun testParseNull() {
        val result = Parser.parse("optional<n>()")
        assertEquals(null, result)

        val result2 = Parser.parse("optional<n>(null)")
        assertEquals(null, result2)
    }

    @Test
    fun testParseObject() {
        val result = Parser.parse("user{id<u32>(12345) name<s64>(Alice)}")
        assertNotNull(result)
        assertTrue(result is Map<*, *>)

        val map = result as Map<*, *>
        assertEquals(12345L, map["id"])
        assertEquals("Alice", map["name"])
    }

    @Test
    fun testParseArray() {
        val result = Parser.parse("tags<s16>[kotlin jvm android]")
        assertNotNull(result)
        assertTrue(result is List<*>)

        val list = result as List<*>
        assertEquals(3, list.size)
        assertEquals("kotlin", list[0])
        assertEquals("jvm", list[1])
        assertEquals("android", list[2])
    }

    @Test
    fun testParseNestedObject() {
        val input = """
            response{
                status<u16>(200)
                data{
                    user{
                        id<u32>(1)
                        name<s32>(Alice)
                    }
                }
            }
        """.trimIndent()

        val result = Parser.parse(input)
        assertNotNull(result)
        assertTrue(result is Map<*, *>)

        val response = result as Map<*, *>
        assertEquals(200, response["status"])

        val data = response["data"] as Map<*, *>
        val user = data["user"] as Map<*, *>
        assertEquals(1L, user["id"])
        assertEquals("Alice", user["name"])
    }

    @Test
    fun testParseArrayOfObjects() {
        val input = """
            users[
                {id<u32>(1) name<s32>(Alice)}
                {id<u32>(2) name<s32>(Bob)}
            ]
        """.trimIndent()

        val result = Parser.parse(input)
        assertNotNull(result)
        assertTrue(result is List<*>)

        val list = result as List<*>
        assertEquals(2, list.size)

        val user1 = list[0] as Map<*, *>
        assertEquals(1L, user1["id"])
        assertEquals("Alice", user1["name"])

        val user2 = list[1] as Map<*, *>
        assertEquals(2L, user2["id"])
        assertEquals("Bob", user2["name"])
    }

    @Test
    fun testParseWithComments() {
        val input = """
            :| This is a comment
            user{
                id<u32>(123) :| User identifier
                name<s64>(Alice) :| User name
            }
        """.trimIndent()

        val result = Parser.parse(input)
        assertNotNull(result)
        assertTrue(result is Map<*, *>)

        val map = result as Map<*, *>
        assertEquals(123L, map["id"])
        assertEquals("Alice", map["name"])
    }

    @Test
    fun testParseAllIntegerTypes() {
        // Signed integers
        assertEquals(127.toByte(), Parser.parse("val<i8>(127)"))
        assertEquals(32767.toShort(), Parser.parse("val<i16>(32767)"))
        assertEquals(2147483647, Parser.parse("val<i32>(2147483647)"))
        assertEquals(9223372036854775807L, Parser.parse("val<i64>(9223372036854775807)"))

        // Unsigned integers (returned as next larger signed type)
        assertEquals(255.toShort(), Parser.parse("val<u8>(255)"))
        assertEquals(65535, Parser.parse("val<u16>(65535)"))
        assertEquals(4294967295L, Parser.parse("val<u32>(4294967295)"))
    }

    @Test
    fun testParseAllStringTypes() {
        assertEquals("Hi", Parser.parse("val<s2>(Hi)"))
        assertEquals("Test", Parser.parse("val<s4>(Test)"))
        assertEquals("Testing", Parser.parse("val<s8>(Testing)"))
        assertEquals("Hello World", Parser.parse("val<s16>(Hello World)"))
    }

    @Test
    fun testParseUtf8String() {
        val result = Parser.parse("city<s16>(北京)")
        assertEquals("北京", result)

        val result2 = Parser.parse("emoji<s8>(Hello🔥)")
        assertEquals("Hello🔥", result2)
    }

    @Test
    fun testParseIntegerOutOfRange() {
        assertFailsWith<GblnError.ParseError> {
            Parser.parse("age<i8>(999)")
        }
    }

    @Test
    fun testParseStringTooLong() {
        assertFailsWith<GblnError.ParseError> {
            Parser.parse("name<s4>(ThisStringIsTooLong)")
        }
    }

    @Test
    fun testParseInvalidSyntax() {
        assertFailsWith<GblnError.ParseError> {
            Parser.parse("user{name<s32>(Alice)")
        }
    }

    @Test
    fun testParseResult() {
        val successResult = Parser.parseResult("age<i8>(25)")
        assertTrue(successResult.isSuccess())
        assertEquals(25.toByte(), (successResult as GblnResult.Success).value)

        val failureResult = Parser.parseResult("age<i8>(999)")
        assertTrue(failureResult.isFailure())
    }

    @Test
    fun testParseAs() {
        val map = Parser.parseAs<Map<String, Any?>>("user{id<u32>(123) name<s64>(Alice)}")
        assertEquals(123L, map["id"])
        assertEquals("Alice", map["name"])

        val list = Parser.parseAs<List<String>>("tags<s16>[kotlin jvm]")
        assertEquals(2, list.size)
    }

    @Test
    fun testParseAsTypeMismatch() {
        assertFailsWith<GblnError.ConversionError> {
            Parser.parseAs<List<*>>("age<i8>(25)")
        }
    }

    @Test
    fun testParseAsync() = runTest {
        val result = Parser.parseAsync("name<s32>(Alice)")
        assertEquals("Alice", result)
    }

    @Test
    fun testParseAsAsync() = runTest {
        val map = Parser.parseAsAsync<Map<String, Any?>>("user{id<u32>(123)}")
        assertEquals(123L, map["id"])
    }

    @Test
    fun testStringExtensionParseGbln() {
        val result = "age<i8>(25)".parseGbln()
        assertEquals(25.toByte(), result)
    }

    @Test
    fun testStringExtensionParseGblnAs() {
        val map = "user{id<u32>(123)}".parseGblnAs<Map<String, Any?>>()
        assertEquals(123L, map["id"])
    }

    @Test
    fun testStringExtensionParseGblnAsync() = runTest {
        val result = "name<s32>(Alice)".parseGblnAsync()
        assertEquals("Alice", result)
    }
}
