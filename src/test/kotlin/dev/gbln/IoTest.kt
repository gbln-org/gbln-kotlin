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
import java.io.File
import java.nio.file.Files
import kotlin.test.*

class IoTest {

    private fun getTempFile(): File {
        val temp = Files.createTempFile("gbln_test", ".gbln").toFile()
        temp.deleteOnExit()
        return temp
    }

    @Test
    fun testWriteAndReadFile() {
        val tempFile = getTempFile()

        val original = mapOf(
            "id" to 123,
            "name" to "Alice",
            "active" to true
        )

        Io.writeFile(tempFile.absolutePath, original)
        assertTrue(tempFile.exists())

        val result = Io.readFile(tempFile.absolutePath)
        assertNotNull(result)
        assertTrue(result is Map<*, *>)

        val map = result as Map<*, *>
        assertEquals(123.toByte(), map["id"])
        assertEquals("Alice", map["name"])
        assertEquals(true, map["active"])
    }

    @Test
    fun testWriteAndReadComplex() {
        val tempFile = getTempFile()

        val original = mapOf(
            "users" to listOf(
                mapOf("id" to 1, "name" to "Alice"),
                mapOf("id" to 2, "name" to "Bob")
            ),
            "total" to 2
        )

        Io.writeFile(tempFile.absolutePath, original)
        val result = Io.readFile(tempFile.absolutePath) as Map<*, *>

        val users = result["users"] as List<*>
        assertEquals(2, users.size)

        val firstUser = users[0] as Map<*, *>
        assertEquals("Alice", firstUser["name"])
    }

    @Test
    fun testReadFileNotFound() {
        assertFailsWith<GblnError.FileNotFound> {
            Io.readFile("/nonexistent/path/to/file.gbln")
        }
    }

    @Test
    fun testWriteFileCreateDirectories() {
        val tempDir = Files.createTempDirectory("gbln_test_dir").toFile()
        tempDir.deleteOnExit()

        val nestedPath = File(tempDir, "subdir/data.gbln")
        nestedPath.deleteOnExit()
        nestedPath.parentFile.deleteOnExit()

        val data = mapOf("test" to "value")
        Io.writeFile(nestedPath.absolutePath, data)

        assertTrue(nestedPath.exists())
        assertTrue(nestedPath.parentFile.exists())
    }

    @Test
    fun testReadFileAsync() = runTest {
        val tempFile = getTempFile()
        val original = mapOf("name" to "Alice")

        Io.writeFile(tempFile.absolutePath, original)
        val result = Io.readFileAsync(tempFile.absolutePath) as Map<*, *>

        assertEquals("Alice", result["name"])
    }

    @Test
    fun testWriteFileAsync() = runTest {
        val tempFile = getTempFile()
        val data = mapOf("name" to "Bob")

        Io.writeFileAsync(tempFile.absolutePath, data)
        assertTrue(tempFile.exists())

        val result = Io.readFile(tempFile.absolutePath) as Map<*, *>
        assertEquals("Bob", result["name"])
    }

    @Test
    fun testReadFileResult() {
        val tempFile = getTempFile()
        val original = mapOf("test" to "data")

        Io.writeFile(tempFile.absolutePath, original)

        val result = Io.readFileResult(tempFile.absolutePath)
        assertTrue(result.isSuccess())

        val data = (result as GblnResult.Success).value as Map<*, *>
        assertEquals("data", data["test"])
    }

    @Test
    fun testReadFileResultNotFound() {
        val result = Io.readFileResult("/nonexistent/file.gbln")
        assertTrue(result.isFailure())

        val error = (result as GblnResult.Failure).error
        assertTrue(error is GblnError.FileNotFound)
    }

    @Test
    fun testWriteFileResult() {
        val tempFile = getTempFile()
        val data = mapOf("test" to "value")

        val result = Io.writeFileResult(tempFile.absolutePath, data)
        assertTrue(result.isSuccess())
        assertTrue(tempFile.exists())
    }

    @Test
    fun testReadFileAs() {
        val tempFile = getTempFile()
        val original = mapOf("id" to 123, "name" to "Alice")

        Io.writeFile(tempFile.absolutePath, original)

        val map = Io.readFileAs<Map<String, Any?>>(tempFile.absolutePath)
        assertEquals("Alice", map["name"])
    }

    @Test
    fun testReadFileAsAsync() = runTest {
        val tempFile = getTempFile()
        val original = mapOf("items" to listOf("a", "b", "c"))

        Io.writeFile(tempFile.absolutePath, original)

        val map = Io.readFileAsAsync<Map<String, Any?>>(tempFile.absolutePath)
        val items = map["items"] as List<*>
        assertEquals(3, items.size)
    }

    @Test
    fun testFileExtensionReadGbln() {
        val tempFile = getTempFile()
        val original = mapOf("test" to "value")

        Io.writeFile(tempFile.absolutePath, original)

        val result = tempFile.readGbln() as Map<*, *>
        assertEquals("value", result["test"])
    }

    @Test
    fun testFileExtensionWriteGbln() {
        val tempFile = getTempFile()
        val data = mapOf("key" to "value")

        tempFile.writeGbln(data)
        assertTrue(tempFile.exists())

        val result = Io.readFile(tempFile.absolutePath) as Map<*, *>
        assertEquals("value", result["key"])
    }

    @Test
    fun testPathExtensionReadGbln() {
        val tempFile = getTempFile()
        val original = mapOf("path" to "test")

        Io.writeFile(tempFile.absolutePath, original)

        val result = tempFile.toPath().readGbln() as Map<*, *>
        assertEquals("test", result["path"])
    }

    @Test
    fun testPathExtensionWriteGbln() {
        val tempFile = getTempFile()
        val data = mapOf("path" to "value")

        tempFile.toPath().writeGbln(data)
        assertTrue(tempFile.exists())
    }

    @Test
    fun testFileExtensionReadGblnAsync() = runTest {
        val tempFile = getTempFile()
        val original = mapOf("async" to "test")

        Io.writeFile(tempFile.absolutePath, original)

        val result = tempFile.readGblnAsync() as Map<*, *>
        assertEquals("test", result["async"])
    }

    @Test
    fun testFileExtensionWriteGblnAsync() = runTest {
        val tempFile = getTempFile()
        val data = mapOf("async" to "write")

        tempFile.writeGblnAsync(data)
        assertTrue(tempFile.exists())
    }

    @Test
    fun testReadFixtureFile() {
        val resourcePath = this::class.java.classLoader.getResource("valid_simple.gbln")
        if (resourcePath != null) {
            val result = Io.readFile(resourcePath.path) as Map<*, *>
            val user = result["user"] as Map<*, *>

            assertEquals("Alice Johnson", user["name"])
            assertEquals(25.toByte(), user["age"])
            assertEquals(true, user["active"])
        }
    }

    @Test
    fun testReadFixtureArray() {
        val resourcePath = this::class.java.classLoader.getResource("valid_array.gbln")
        if (resourcePath != null) {
            val result = Io.readFile(resourcePath.path) as Map<*, *>
            val data = result["data"] as Map<*, *>
            val tags = data["tags"] as List<*>

            assertEquals(4, tags.size)
            assertEquals("kotlin", tags[0])
        }
    }

    @Test
    fun testReadFixtureNested() {
        val resourcePath = this::class.java.classLoader.getResource("valid_nested.gbln")
        if (resourcePath != null) {
            val result = Io.readFile(resourcePath.path) as Map<*, *>
            val response = result["response"] as Map<*, *>
            val data = response["data"] as Map<*, *>
            val users = data["users"] as List<*>

            assertEquals(3, users.size)
            assertEquals(200, response["status"])
        }
    }
}
