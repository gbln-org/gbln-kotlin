// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IoTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test readIo with simple file`() {
        // Given
        val fixturePath = javaClass.getResource("/fixtures/valid/simple.gbln")?.path
        assertNotNull(fixturePath, "Fixture file not found")

        // When
        val data = readIo(fixturePath)

        // Then
        assertNotNull(data)
        assertTrue(data is Map<*, *>)

        @Suppress("UNCHECKED_CAST")
        val user = data as Map<String, Any?>
        assertEquals(12345L, user["id"])
        assertEquals("Alice", user["name"])
        assertEquals(25L, user["age"])
        assertEquals(true, user["active"])
    }

    @Test
    fun `test readIo with nested objects`() {
        // Given
        val fixturePath = javaClass.getResource("/fixtures/valid/nested.gbln")?.path
        assertNotNull(fixturePath, "Fixture file not found")

        // When
        val data = readIo(fixturePath)

        // Then
        assertNotNull(data)
        assertTrue(data is Map<*, *>)

        @Suppress("UNCHECKED_CAST")
        val config = data as Map<String, Any?>
        assertTrue(config.containsKey("database"))
        assertTrue(config.containsKey("server"))

        @Suppress("UNCHECKED_CAST")
        val database = config["database"] as Map<String, Any?>
        assertEquals("localhost", database["host"])
        assertEquals(5432L, database["port"])

        @Suppress("UNCHECKED_CAST")
        val credentials = database["credentials"] as Map<String, Any?>
        assertEquals("admin", credentials["username"])
        assertEquals("secret123", credentials["password"])
    }

    @Test
    fun `test writeIo and readIo roundtrip uncompressed`() {
        // Given
        val testFile = tempDir.resolve("test.io.gbln")
        val original = parseRaw("user{id<u32>(999)name<s64>(Bob)}")
        val config = GblnConfig(compress = false)

        // When - Write
        writeIo(original, testFile, config)
        assertTrue(testFile.toFile().exists(), "File should exist after write")

        // Then - Read back
        val data = readIo(testFile)

        assertNotNull(data)
        assertTrue(data is Map<*, *>)

        @Suppress("UNCHECKED_CAST")
        val user = data as Map<String, Any?>
        assertEquals(999L, user["id"])
        assertEquals("Bob", user["name"])
    }

    @Test
    fun `test writeIo with default config (compressed)`() {
        // Given
        val testFile = tempDir.resolve("test.io.gbln.xz")
        val original = parseRaw("config{debug<b>(t)workers<u8>(8)}")

        // When
        writeIo(original, testFile)  // Uses default config (compressed)
        assertTrue(testFile.toFile().exists(), "File should exist after write")

        // Then - Read back (auto-decompresses)
        val data = readIo(testFile)

        assertNotNull(data)
        assertTrue(data is Map<*, *>)

        @Suppress("UNCHECKED_CAST")
        val config = data as Map<String, Any?>
        assertEquals(true, config["debug"])
        assertEquals(8L, config["workers"])
    }

    @Test
    fun `test writeIo with Path object`() {
        // Given
        val testFile = tempDir.resolve("path-test.io.gbln")
        val original = parseRaw("test{value<u32>(42)}")
        val config = GblnConfig(compress = false)

        // When
        writeIo(original, testFile, config)  // Path object

        // Then
        val data = readIo(testFile)  // Path object

        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val test = data as Map<String, Any?>
        assertEquals(42L, test["value"])
    }

    @Test
    fun `test writeIo with custom compression level`() {
        // Given
        val testFile = tempDir.resolve("compressed.io.gbln.xz")
        val original = parseRaw("data{items<u16>[1 2 3 4 5]}")
        val config = GblnConfig(compress = true, compressionLevel = 9)

        // When
        writeIo(original, testFile, config)
        assertTrue(testFile.toFile().exists())

        // Then - Read back
        val data = readIo(testFile)

        assertNotNull(data)
        @Suppress("UNCHECKED_CAST")
        val result = data as Map<String, Any?>
        assertTrue(result.containsKey("items"))
    }

    @Test
    fun `test readIo with non-existent file throws IoError`() {
        // Given
        val nonExistentPath = tempDir.resolve("does-not-exist.gbln")

        // When/Then
        try {
            readIo(nonExistentPath)
            throw AssertionError("Expected IoError to be thrown")
        } catch (e: IoError) {
            // Expected
            assertTrue(e.message?.contains("error") == true || e.message?.contains("Error") == true)
        }
    }
}
