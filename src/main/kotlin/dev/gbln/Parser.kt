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

import com.sun.jna.ptr.PointerByReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * GBLN parser API.
 *
 * Provides functions to parse GBLN strings and files into Kotlin values.
 * Pattern follows bindings/python/src/gbln/parse.py exactly.
 */

/**
 * Parse GBLN string to raw managed value (low-level API).
 *
 * Returns a ManagedGblnValue that can be passed to writeIo() or serialised.
 * Use parse() instead if you want automatic conversion to Kotlin types.
 *
 * @param gblnString GBLN-formatted string
 * @return ManagedGblnValue with automatic memory cleanup
 * @throws ParseError if parsing fails
 */
fun parseRaw(gblnString: String): ManagedGblnValue {
    // Prepare output pointer
    val valuePtr = PointerByReference()

    // Call C function
    val errorCode = lib.gbln_parse(gblnString, valuePtr)

    // Check for errors
    if (errorCode != GblnErrorCode.OK) {
        throw ParseError("Parse failed with error code: $errorCode")
    }

    // Wrap in managed value for automatic cleanup
    return ManagedGblnValue(valuePtr.value)
}

/**
 * Parse GBLN string to Kotlin value.
 *
 * @param gblnString GBLN-formatted string
 * @return Kotlin Map, List, or primitive value
 * @throws ParseError if parsing fails
 */
fun parse(gblnString: String): Any? {
    val managedValue = parseRaw(gblnString)
    return gblnToKotlin(managedValue.ptr)
}

/**
 * Parse GBLN file to Kotlin value.
 *
 * @param filePath Path to .gbln file
 * @return Kotlin Map, List, or primitive value
 * @throws GblnError if parsing fails
 * @throws java.io.FileNotFoundException if file doesn't exist
 * @throws java.io.IOException if file cannot be read
 */
fun parseFile(filePath: String): Any? = parseFile(Paths.get(filePath))

/**
 * Parse GBLN file to Kotlin value.
 *
 * @param filePath Path to .gbln file
 * @return Kotlin Map, List, or primitive value
 * @throws GblnError if parsing fails
 * @throws java.io.FileNotFoundException if file doesn't exist
 * @throws java.io.IOException if file cannot be read
 */
fun parseFile(filePath: Path): Any? {
    if (!Files.exists(filePath)) {
        throw java.io.FileNotFoundException("File not found: $filePath")
    }

    if (!Files.isRegularFile(filePath)) {
        throw java.io.IOException("Not a file: $filePath")
    }

    val content = try {
        Files.readString(filePath)
    } catch (e: Exception) {
        throw java.io.IOException("Failed to read file $filePath: ${e.message}", e)
    }

    return parse(content)
}
