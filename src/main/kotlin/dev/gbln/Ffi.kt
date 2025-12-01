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

import com.sun.jna.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * GBLN FFI layer using JNA.
 *
 * Loads libgbln shared library and provides typed wrappers for all C functions.
 * Handles platform-specific library loading and memory management.
 *
 * Pattern follows bindings/python/src/gbln/ffi.py exactly.
 */

// Error codes from gbln.h
object GblnErrorCode {
    const val OK = 0
    const val ERROR_UNEXPECTED_CHAR = 1
    const val ERROR_UNTERMINATED_STRING = 2
    const val ERROR_UNEXPECTED_TOKEN = 3
    const val ERROR_UNEXPECTED_EOF = 4
    const val ERROR_INVALID_SYNTAX = 5
    const val ERROR_INT_OUT_OF_RANGE = 6
    const val ERROR_STRING_TOO_LONG = 7
    const val ERROR_TYPE_MISMATCH = 8
    const val ERROR_INVALID_TYPE_HINT = 9
    const val ERROR_DUPLICATE_KEY = 10
    const val ERROR_NULL_POINTER = 11
    const val ERROR_IO = 12
}

// Value types from gbln.h
object GblnValueType {
    const val I8 = 0
    const val I16 = 1
    const val I32 = 2
    const val I64 = 3
    const val U8 = 4
    const val U16 = 5
    const val U32 = 6
    const val U64 = 7
    const val F32 = 8
    const val F64 = 9
    const val BOOL = 10
    const val STRING = 11
    const val NULL = 12
    const val OBJECT = 13
    const val ARRAY = 14
}

// Opaque pointer type
class GblnValue : PointerType()

/**
 * Find libgbln shared library.
 *
 * Search order (same as Python):
 * 1. GBLN_LIBRARY_PATH environment variable
 * 2. Alongside package (bundled in JAR)
 * 3. core/ffi/libs/{platform}/ (pre-built committed libraries)
 * 4. System library paths
 */
private fun findLibrary(): Path? {
    // Determine platform-specific library directory and name
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    val (libDir, libName) = when {
        osName.contains("linux") -> {
            when {
                osArch in listOf("x86_64", "amd64") -> "linux-x64" to "libgbln.so"
                osArch in listOf("aarch64", "arm64") -> "linux-arm64" to "libgbln.so"
                else -> null to "libgbln.so"
            }
        }
        osName.contains("mac") || osName.contains("darwin") -> {
            when {
                osArch == "aarch64" || osArch == "arm64" -> "macos-arm64" to "libgbln.dylib"
                osArch in listOf("x86_64", "amd64") -> "macos-x64" to "libgbln.dylib"
                else -> null to "libgbln.dylib"
            }
        }
        osName.contains("windows") -> {
            when {
                osArch in listOf("amd64", "x86_64") -> "windows-x64" to "gbln.dll"
                else -> null to "gbln.dll"
            }
        }
        osName.contains("freebsd") -> {
            when {
                osArch == "amd64" -> "freebsd-x64" to "libgbln.so"
                osArch == "aarch64" -> "freebsd-arm64" to "libgbln.so"
                else -> null to "libgbln.so"
            }
        }
        else -> null to "libgbln.so"
    }

    // 1. Try environment variable first
    System.getenv("GBLN_LIBRARY_PATH")?.let { envPath ->
        val libPath = Paths.get(envPath)
        if (libPath.toFile().exists()) {
            return libPath
        }
    }

    // 2. Try alongside package (bundled in JAR)
    // Extract from JAR resources if needed
    try {
        val resourcePath = "/native/$libDir/$libName"
        val resource = object {}.javaClass.getResourceAsStream(resourcePath)
        if (resource != null) {
            // Extract to temp file
            val tempFile = File.createTempFile("libgbln", libName.substringAfterLast('.'))
            tempFile.deleteOnExit()
            resource.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile.toPath()
        }
    } catch (e: Exception) {
        // Resource not found in JAR, continue
    }

    // 3. Try core/ffi/libs/{platform}/ (pre-built committed libraries)
    if (libDir != null) {
        // Find project root by looking for core/ffi/libs/
        var currentDir = File(System.getProperty("user.dir"))
        var attempts = 0
        while (attempts < 10) {
            val libsPath = currentDir.resolve("core/ffi/libs/$libDir/$libName")
            if (libsPath.exists()) {
                return libsPath.toPath()
            }
            val parent = currentDir.parentFile ?: break
            currentDir = parent
            attempts++
        }

        // Also try from class location
        try {
            val classPath = File(object {}.javaClass.protectionDomain.codeSource.location.toURI())
            var searchDir = if (classPath.isFile) classPath.parentFile else classPath
            attempts = 0
            while (attempts < 10) {
                val libsPath = searchDir.resolve("../../core/ffi/libs/$libDir/$libName")
                if (libsPath.exists()) {
                    return libsPath.canonicalFile.toPath()
                }
                val parent = searchDir.parentFile ?: break
                searchDir = parent
                attempts++
            }
        } catch (e: Exception) {
            // Continue to next search method
        }
    }

    // 4. Let JNA try system paths
    return null
}

/**
 * Load libgbln shared library using JNA.
 */
private fun loadLibrary(): GblnLibrary {
    val libPath = findLibrary()

    return if (libPath != null) {
        try {
            Native.load(libPath.toString(), GblnLibrary::class.java) as GblnLibrary
        } catch (e: UnsatisfiedLinkError) {
            throw IoError("Failed to load GBLN library from $libPath: ${e.message}")
        }
    } else {
        // Try system library as last resort
        try {
            Native.load("gbln", GblnLibrary::class.java) as GblnLibrary
        } catch (e: UnsatisfiedLinkError) {
            throw IoError(
                "Failed to locate GBLN library. " +
                "Please ensure libgbln is installed or set GBLN_LIBRARY_PATH environment variable. " +
                "Error: ${e.message}"
            )
        }
    }
}

/**
 * JNA interface to libgbln C functions.
 * Maps exactly to gbln.h function signatures.
 */
interface GblnLibrary : Library {
    // Parser
    fun gbln_parse(input: String, outValue: com.sun.jna.ptr.PointerByReference): Int
    fun gbln_parse_file(path: String, outValue: com.sun.jna.ptr.PointerByReference): Int

    // Serialiser
    fun gbln_to_string(value: Pointer): Pointer
    fun gbln_to_string_pretty(value: Pointer): Pointer
    fun gbln_write_file(path: String, value: Pointer): Int

    // Memory
    fun gbln_value_free(value: Pointer)

    // Type query
    fun gbln_value_type(value: Pointer): Int

    // Value getters (with ok flag)
    fun gbln_value_as_i8(value: Pointer, ok: ByteArray): Byte
    fun gbln_value_as_i16(value: Pointer, ok: ByteArray): Short
    fun gbln_value_as_i32(value: Pointer, ok: ByteArray): Int
    fun gbln_value_as_i64(value: Pointer, ok: ByteArray): Long
    fun gbln_value_as_u8(value: Pointer, ok: ByteArray): Short
    fun gbln_value_as_u16(value: Pointer, ok: ByteArray): Int
    fun gbln_value_as_u32(value: Pointer, ok: ByteArray): Long
    fun gbln_value_as_u64(value: Pointer, ok: ByteArray): Long
    fun gbln_value_as_f32(value: Pointer, ok: ByteArray): Float
    fun gbln_value_as_f64(value: Pointer, ok: ByteArray): Double
    fun gbln_value_as_bool(value: Pointer, ok: ByteArray): Byte
    fun gbln_value_as_string(value: Pointer, ok: ByteArray): Pointer

    // Object operations
    fun gbln_object_get(obj: Pointer, key: String): Pointer
    fun gbln_object_len(obj: Pointer): Long
    fun gbln_object_keys(obj: Pointer, outCount: com.sun.jna.ptr.LongByReference): Pointer

    // Array operations
    fun gbln_array_get(array: Pointer, index: Long): Pointer
    fun gbln_array_len(array: Pointer): Long

    // I/O operations
    fun gbln_read_io(path: String, outValue: com.sun.jna.ptr.PointerByReference): Int
    fun gbln_write_io(value: Pointer, path: String, config: Pointer): Int

    // Configuration
    fun gbln_config_new(miniMode: Boolean, compress: Boolean, compressionLevel: Int, indent: Int, stripComments: Boolean): Pointer
    fun gbln_config_new_io(): Pointer
    fun gbln_config_free(config: Pointer)

    // Error handling
    fun gbln_last_error_message(): Pointer
    fun gbln_string_free(str: Pointer)
}

/**
 * Global library instance (lazy-loaded).
 */
internal val lib: GblnLibrary by lazy { loadLibrary() }
