// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull

class ObjectTest {

    @Test
    fun `test parse simple object`() {
        // Given
        val input = "user{id<u32>(12345)name<s64>(Alice)}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        assertTrue(data is Map<*, *>)

        @Suppress("UNCHECKED_CAST")
        val user = data as Map<String, Any?>
        assertEquals(12345L, user["id"])
        assertEquals("Alice", user["name"])
    }

    @Test
    fun `test parse nested objects`() {
        // Given
        val input = "config{database{host<s64>(localhost)port<u16>(5432)}}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val config = data as Map<String, Any?>
        assertTrue(config.containsKey("database"))

        @Suppress("UNCHECKED_CAST")
        val database = config["database"] as Map<String, Any?>
        assertEquals("localhost", database["host"])
        assertEquals(5432L, database["port"])
    }

    @Test
    fun `test parse object with multiple nested levels`() {
        // Given
        val input = "root{level1{level2{level3{value<u32>(999)}}}}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val root = data as Map<String, Any?>

        @Suppress("UNCHECKED_CAST")
        val level1 = root["level1"] as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val level2 = level1["level2"] as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val level3 = level2["level3"] as Map<String, Any?>

        assertEquals(999L, level3["value"])
    }

    @Test
    fun `test parse object with mixed types`() {
        // Given
        val input = "person{id<u32>(1)name<s64>(Bob)age<i8>(30)height<f32>(1.75)active<b>(t)nickname<n>()}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val person = data as Map<String, Any?>

        assertEquals(1L, person["id"])
        assertEquals("Bob", person["name"])
        assertEquals(30L, person["age"])
        assertTrue(person["height"] is Double || person["height"] is Float)
        assertEquals(true, person["active"])
        assertNull(person["nickname"])
    }

    @Test
    fun `test parse empty object`() {
        // Given
        val input = "empty{}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val empty = data as Map<String, Any?>
        assertTrue(empty.isEmpty())
    }

    @Test
    fun `test parse object with array fields`() {
        // Given
        val input = "data{tags<s16>[a b c]scores<u8>[1 2 3]}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>

        assertTrue(obj["tags"] is List<*>)
        assertTrue(obj["scores"] is List<*>)

        @Suppress("UNCHECKED_CAST")
        val tags = obj["tags"] as List<String>
        assertEquals(listOf("a", "b", "c"), tags)

        @Suppress("UNCHECKED_CAST")
        val scores = obj["scores"] as List<Long>
        assertEquals(listOf(1L, 2L, 3L), scores)
    }

    @Test
    fun `test parse object with comments`() {
        // Given
        val input = """
            :| User configuration
            user{
                :| User ID
                id<u32>(42)
                :| Username
                name<s64>(Alice)
            }
        """.trimIndent()

        // When
        val data = parse(input)

        // Then - Comments are stripped during parsing
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val user = data as Map<String, Any?>
        assertEquals(42L, user["id"])
        assertEquals("Alice", user["name"])
    }

    @Test
    fun `test parse object with special characters in strings`() {
        // Given
        val input = "test{html<s256>(<h1>Title</h1>)url<s128>(https://example.com/path?q=1)}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val test = data as Map<String, Any?>
        assertEquals("<h1>Title</h1>", test["html"])
        assertEquals("https://example.com/path?q=1", test["url"])
    }

    @Test
    fun `test parse object keys are case sensitive`() {
        // Given
        val input = "test{Name<s32>(Alice)name<s32>(Bob)NAME<s32>(Charlie)}"

        // When
        val data = parse(input)

        // Then - All three keys should exist
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val test = data as Map<String, Any?>

        assertEquals("Alice", test["Name"])
        assertEquals("Bob", test["name"])
        assertEquals("Charlie", test["NAME"])
        assertEquals(3, test.size)
    }

    @Test
    fun `test parse object with underscores and numbers in keys`() {
        // Given
        val input = "data{field_1<u32>(1)field_2<u32>(2)var123<u32>(123)}"

        // When
        val data = parse(input)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>

        assertEquals(1L, obj["field_1"])
        assertEquals(2L, obj["field_2"])
        assertEquals(123L, obj["var123"])
    }

    @Test
    fun `test object roundtrip maintains structure`() {
        // Given
        val original = "complex{a<u32>(1)b{c<u32>(2)d<u32>(3)}e<u32>(4)}"

        // When
        val value = parseRaw(original)
        val serialised = toString(value)
        val data = parse(serialised)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val complex = data as Map<String, Any?>

        assertEquals(1L, complex["a"])
        assertTrue(complex["b"] is Map<*, *>)
        assertEquals(4L, complex["e"])

        @Suppress("UNCHECKED_CAST")
        val b = complex["b"] as Map<String, Any?>
        assertEquals(2L, b["c"])
        assertEquals(3L, b["d"])
    }
}
