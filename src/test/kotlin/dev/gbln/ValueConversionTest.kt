// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValueConversionTest {

    @Test
    fun `test convert signed integers`() {
        val data = parse("ints{i8<i8>(-128)i16<i16>(-32768)i32<i32>(-2147483648)i64<i64>(-9223372036854775807)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val ints = data as Map<String, Any?>
        assertEquals(-128L, ints["i8"])
        assertEquals(-32768L, ints["i16"])
        assertEquals(-2147483648L, ints["i32"])
        assertEquals(-9223372036854775807L, ints["i64"])
    }

    @Test
    fun `test convert unsigned integers`() {
        val data = parse("uints{u8<u8>(255)u16<u16>(65535)u32<u32>(4294967295)u64<u64>(9223372036854775807)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val uints = data as Map<String, Any?>
        assertEquals(255L, uints["u8"])
        assertEquals(65535L, uints["u16"])
        assertEquals(4294967295L, uints["u32"])
        assertTrue(uints["u64"] is Long)
    }

    @Test
    fun `test convert floats`() {
        val data = parse("floats{f32<f32>(3.14159)f64<f64>(2.718281828459045)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val floats = data as Map<String, Any?>
        assertTrue(floats["f32"] is Double || floats["f32"] is Float)
        assertTrue(floats["f64"] is Double)
        val f32Val = (floats["f32"] as Number).toDouble()
        val f64Val = floats["f64"] as Double
        assertEquals(3.14159, f32Val, 0.0001)
        assertEquals(2.718281828459045, f64Val, 0.000000000001)
    }

    @Test
    fun `test convert strings`() {
        val data = parse("strings{short<s8>(Hi)long<s128>(This is a much longer string with spaces!)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val strings = data as Map<String, Any?>
        assertEquals("Hi", strings["short"])
        assertEquals("This is a much longer string with spaces!", strings["long"])
    }

    @Test
    fun `test convert booleans`() {
        val data = parse("bools{t_short<b>(t)f_short<b>(f)t_long<b>(true)f_long<b>(false)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val bools = data as Map<String, Any?>
        assertEquals(true, bools["t_short"])
        assertEquals(false, bools["f_short"])
        assertEquals(true, bools["t_long"])
        assertEquals(false, bools["f_long"])
    }

    @Test
    fun `test convert null values`() {
        val data = parse("nulls{null1<n>()null2<n>(null)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val nulls = data as Map<String, Any?>
        assertNull(nulls["null1"])
        assertNull(nulls["null2"])
    }

    @Test
    fun `test convert arrays of integers`() {
        val data = parse("arrays{i8s<i8>[-1 0 1]u32s<u32>[1 2 3 4 5]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val arrays = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val i8s = arrays["i8s"] as List<Long>
        assertEquals(listOf(-1L, 0L, 1L), i8s)
        @Suppress("UNCHECKED_CAST")
        val u32s = arrays["u32s"] as List<Long>
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), u32s)
    }

    @Test
    fun `test convert arrays of strings`() {
        val data = parse("data{tags<s16>[rust kotlin go python]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val tags = obj["tags"] as List<String>
        assertEquals(listOf("rust", "kotlin", "go", "python"), tags)
    }

    @Test
    fun `test convert arrays of floats`() {
        val data = parse("data{prices<f32>[19.99 29.99 9.99]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val prices = obj["prices"] as List<Double>
        assertEquals(3, prices.size)
        assertEquals(19.99, prices[0], 0.01)
        assertEquals(29.99, prices[1], 0.01)
        assertEquals(9.99, prices[2], 0.01)
    }

    @Test
    fun `test convert arrays of booleans`() {
        val data = parse("data{flags<b>[t f t t f]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val flags = obj["flags"] as List<Boolean>
        assertEquals(listOf(true, false, true, true, false), flags)
    }

    @Test
    fun `test convert empty array`() {
        val data = parse("data{empty<u32>[]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val obj = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val empty = obj["empty"] as List<*>
        assertTrue(empty.isEmpty())
    }

    @Test
    fun `test convert nested maps`() {
        val data = parse("outer{middle{inner{value<u32>(999)}}}")
        assertNotNull(data)
        assertTrue(data is Map<*, *>)
        @Suppress("UNCHECKED_CAST")
        val outer = data as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val middle = outer["middle"] as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val inner = middle["inner"] as Map<String, Any?>
        assertEquals(999L, inner["value"])
    }

    @Test
    fun `test convert mixed types in object`() {
        val data = parse("mixed{num<u32>(42)text<s32>(hello)flag<b>(t)nothing<n>()nested{sub<u8>(8)}list<u16>[1 2 3]}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val mixed = data as Map<String, Any?>
        assertEquals(42L, mixed["num"])
        assertEquals("hello", mixed["text"])
        assertEquals(true, mixed["flag"])
        assertNull(mixed["nothing"])
        assertTrue(mixed["nested"] is Map<*, *>)
        assertTrue(mixed["list"] is List<*>)
        @Suppress("UNCHECKED_CAST")
        val nested = mixed["nested"] as Map<String, Any?>
        assertEquals(8L, nested["sub"])
        @Suppress("UNCHECKED_CAST")
        val list = mixed["list"] as List<Long>
        assertEquals(listOf(1L, 2L, 3L), list)
    }

    @Test
    fun `test convert preserves numeric precision`() {
        val data = parse("precision{max_i64<i64>(9223372036854775807)max_u32<u32>(4294967295)pi<f64>(3.141592653589793)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val precision = data as Map<String, Any?>
        assertEquals(9223372036854775807L, precision["max_i64"])
        assertEquals(4294967295L, precision["max_u32"])
        val pi = precision["pi"] as Double
        assertEquals(3.141592653589793, pi, 0.0000000000001)
    }

    @Test
    fun `test convert UTF-8 strings`() {
        val data = parse("unicode{emoji<s32>(Hello🔥)chinese<s32>(你好)german<s32>(Größe)}")
        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val unicode = data as Map<String, Any?>
        assertEquals("Hello🔥", unicode["emoji"])
        assertEquals("你好", unicode["chinese"])
        assertEquals("Größe", unicode["german"])
    }
}
