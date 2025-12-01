// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SerialiserTest {

    @Test
    fun `test toString serialises to MINI format`() {
        // Given
        val input = "user{id<u32>(12345)name<s64>(Alice)age<i8>(25)}"
        val value = parseRaw(input)

        // When
        val serialised = toString(value)

        // Then
        assertNotNull(serialised)
        // MINI format has no whitespace
        assertTrue(serialised.contains("user{"))
        assertTrue(serialised.contains("id<u32>(12345)"))
        assertTrue(serialised.contains("name<s64>(Alice)"))
        assertTrue(serialised.contains("age<i8>(25)"))
    }

    @Test
    fun `test toStringPretty adds formatting`() {
        // Given
        val input = "user{id<u32>(999)name<s64>(Bob)}"
        val value = parseRaw(input)

        // When
        val pretty = toStringPretty(value)

        // Then
        assertNotNull(pretty)
        // Pretty format has newlines and indentation
        assertTrue(pretty.contains("\n"), "Pretty format should have newlines")
        assertTrue(pretty.contains("user{"))
        assertTrue(pretty.contains("id<u32>(999)"))
        assertTrue(pretty.contains("name<s64>(Bob)"))
    }

    @Test
    fun `test roundtrip parse and serialise`() {
        // Given
        val original = "config{debug<b>(t)workers<u8>(4)port<u16>(8080)}"

        // When
        val value = parseRaw(original)
        val serialised = toString(value)
        val data = parse(serialised)

        // Then
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val config = data as Map<String, Any?>
        assertEquals(true, config["debug"])
        assertEquals(4L, config["workers"])
        assertEquals(8080L, config["port"])
    }

    @Test
    fun `test serialise nested objects`() {
        // Given
        val input = "outer{inner{value<u32>(42)}}"
        val value = parseRaw(input)

        // When
        val serialised = toString(value)

        // Then
        assertNotNull(serialised)
        assertTrue(serialised.contains("outer{"))
        assertTrue(serialised.contains("inner{"))
        assertTrue(serialised.contains("value<u32>(42)"))
    }

    @Test
    fun `test serialise arrays`() {
        // Given
        val input = "data{tags<s16>[rust kotlin go]scores<u8>[95 87 92]}"
        val value = parseRaw(input)

        // When
        val serialised = toString(value)

        // Then
        assertNotNull(serialised)
        assertTrue(serialised.contains("tags<s16>["))
        assertTrue(serialised.contains("rust"))
        assertTrue(serialised.contains("kotlin"))
        assertTrue(serialised.contains("go"))
        assertTrue(serialised.contains("scores<u8>["))
        assertTrue(serialised.contains("95"))
        assertTrue(serialised.contains("87"))
        assertTrue(serialised.contains("92"))
    }

    @Test
    fun `test serialise all types`() {
        // Given
        val input = """
            types{
                i8_val<i8>(-128)
                u32_val<u32>(12345)
                f32_val<f32>(3.14)
                str_val<s32>(test)
                bool_val<b>(t)
                null_val<n>()
            }
        """.trimIndent().replace("\n", "").replace(" ", "")

        val value = parseRaw(input)

        // When
        val serialised = toString(value)

        // Then
        assertNotNull(serialised)
        assertTrue(serialised.contains("i8_val<i8>(-128)"))
        assertTrue(serialised.contains("u32_val<u32>(12345)"))
        assertTrue(serialised.contains("f32_val<f32>(3.14"))  // Float might have precision
        assertTrue(serialised.contains("str_val<s32>(test)"))
        assertTrue(serialised.contains("bool_val<b>(t)"))
        assertTrue(serialised.contains("null_val<n>()"))
    }

    @Test
    fun `test pretty format has proper indentation`() {
        // Given
        val input = "root{child1{grandchild<u32>(1)}child2{grandchild<u32>(2)}}"
        val value = parseRaw(input)

        // When
        val pretty = toStringPretty(value)

        // Then
        assertNotNull(pretty)
        // Check for indentation (should have multiple levels)
        val lines = pretty.lines()
        assertTrue(lines.size > 5, "Pretty format should have multiple lines")

        // At least one line should start with whitespace (indentation)
        assertTrue(lines.any { it.startsWith("  ") || it.startsWith("\t") },
            "Pretty format should have indented lines")
    }

    @Test
    fun `test serialise empty object`() {
        // Given
        val input = "empty{}"
        val value = parseRaw(input)

        // When
        val serialised = toString(value)

        // Then
        assertNotNull(serialised)
        assertTrue(serialised.contains("empty{") && serialised.contains("}"))
    }

    @Test
    fun `test roundtrip preserves type information`() {
        // Given - Various integer types
        val original = "ints{i8<i8>(127)u8<u8>(255)i32<i32>(2147483647)}"

        // When
        val value = parseRaw(original)
        val serialised = toString(value)

        // Then - Type hints should be preserved
        assertTrue(serialised.contains("<i8>"))
        assertTrue(serialised.contains("<u8>"))
        assertTrue(serialised.contains("<i32>"))

        // And values should roundtrip correctly
        val data = parse(serialised)
        assertNotNull(data)
    }
}
