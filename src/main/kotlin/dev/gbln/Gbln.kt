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

/**
 * GBLN (Goblin Bounded Lean Notation) - Public API Entry Point
 *
 * GBLN is the first type-safe LLM-native serialisation format designed for:
 * - **Token efficiency**: 86% fewer tokens than JSON in LLM contexts
 * - **Type safety**: Parse-time validation with inline type hints
 * - **Memory efficiency**: Bounded types prevent waste and vulnerabilities
 * - **Human readability**: Text-based format with clear syntax
 * - **Git-friendliness**: Meaningful diffs, ordered keys preserved
 *
 * # Basic Usage
 *
 * ```kotlin
 * // Parse GBLN string
 * val data = Gbln.parse("user{id<u32>(12345) name<s64>(Alice)}")
 *
 * // Serialise Kotlin value to GBLN
 * val gbln = Gbln.toString(mapOf("id" to 12345, "name" to "Alice"))
 *
 * // Read from file
 * val config = Gbln.readFile("config.gbln")
 *
 * // Write to file
 * Gbln.writeFile("output.gbln", data)
 * ```
 *
 * # Type System
 *
 * - **Integers**: i8, i16, i32, i64, u8, u16, u32, u64
 * - **Floats**: f32, f64
 * - **Strings**: s2, s4, s8, s16, s32, s64, s128, s256, s512, s1024
 * - **Boolean**: b (true/false or t/f)
 * - **Null**: n (null or empty)
 * - **Object**: {...}
 * - **Array**: [...]
 *
 * # Examples
 *
 * ```kotlin
 * // Simple values
 * Gbln.parse("age<i8>(25)")                    // 25 (Byte)
 * Gbln.parse("price<f32>(19.99)")              // 19.99 (Float)
 * Gbln.parse("name<s32>(Alice)")               // "Alice" (String)
 * Gbln.parse("active<b>(t)")                   // true (Boolean)
 *
 * // Objects
 * Gbln.parse("user{id<u32>(123) name<s64>(Alice)}")
 * // Result: mapOf("id" to 123, "name" to "Alice")
 *
 * // Arrays
 * Gbln.parse("tags<s16>[kotlin jvm android]")
 * // Result: listOf("kotlin", "jvm", "android")
 *
 * // Nested structures
 * Gbln.parse("""
 *     response{
 *         status<u16>(200)
 *         data{
 *             users[
 *                 {id<u32>(1) name<s32>(Alice)}
 *                 {id<u32>(2) name<s32>(Bob)}
 *             ]
 *         }
 *     }
 * """)
 * ```
 *
 * # Async Operations
 *
 * ```kotlin
 * // Parse asynchronously
 * val data = Gbln.parseAsync(gblnString)
 *
 * // Read file asynchronously
 * val config = Gbln.readFileAsync("config.gbln")
 *
 * // Write file asynchronously
 * Gbln.writeFileAsync("output.gbln", data)
 * ```
 *
 * # Configuration
 *
 * ```kotlin
 * // Pretty-print output
 * val pretty = Gbln.toPrettyString(data)
 *
 * // Use custom config
 * val config = GblnConfig(
 *     prettyPrint = true,
 *     strictTypes = true,
 *     checkDuplicateKeys = true
 * )
 *
 * val parsed = Gbln.parse(input, config)
 * ```
 *
 * # Error Handling
 *
 * ```kotlin
 * // Using exceptions
 * try {
 *     val data = Gbln.parse(input)
 * } catch (e: GblnError.ParseError) {
 *     println("Parse failed: ${e.message}")
 * }
 *
 * // Using Result type
 * val result = Gbln.parseResult(input)
 * result.onSuccess { data ->
 *     println("Success: $data")
 * }.onFailure { error ->
 *     println("Error: ${error.message}")
 * }
 * ```
 *
 * @see Parser for parsing operations
 * @see Serialiser for serialisation operations
 * @see Io for file I/O operations
 * @see GblnConfig for configuration options
 * @see GblnError for error types
 */
object Gbln {

    /**
     * Library version string.
     */
    const val VERSION = "0.1.0"

    /**
     * GBLN specification version.
     */
    const val SPEC_VERSION = "1.0"

    // Parsing functions (delegate to Parser)

    /**
     * Parse GBLN string synchronously.
     * @see Parser.parse
     */
    fun parse(input: String, config: GblnConfig = GblnConfig.DEFAULT): Any? =
        Parser.parse(input, config)

    /**
     * Parse GBLN string asynchronously.
     * @see Parser.parseAsync
     */
    suspend fun parseAsync(input: String, config: GblnConfig = GblnConfig.DEFAULT): Any? =
        Parser.parseAsync(input, config)

    /**
     * Parse GBLN string and return Result type.
     * @see Parser.parseResult
     */
    fun parseResult(input: String, config: GblnConfig = GblnConfig.DEFAULT): GblnResult<Any?> =
        Parser.parseResult(input, config)

    /**
     * Parse GBLN string and cast to specific type.
     * @see Parser.parseAs
     */
    inline fun <reified T> parseAs(input: String, config: GblnConfig = GblnConfig.DEFAULT): T =
        Parser.parseAs(input, config)

    /**
     * Parse GBLN string asynchronously and cast to specific type.
     * @see Parser.parseAsAsync
     */
    suspend inline fun <reified T> parseAsAsync(
        input: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T = Parser.parseAsAsync(input, config)

    // Serialisation functions (delegate to Serialiser)

    /**
     * Serialise Kotlin value to compact GBLN string.
     * @see Serialiser.toString
     */
    fun toString(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String =
        Serialiser.toString(value, config)

    /**
     * Serialise Kotlin value to pretty-printed GBLN string.
     * @see Serialiser.toPrettyString
     */
    fun toPrettyString(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String =
        Serialiser.toPrettyString(value, config)

    /**
     * Serialise Kotlin value asynchronously.
     * @see Serialiser.toStringAsync
     */
    suspend fun toStringAsync(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String =
        Serialiser.toStringAsync(value, config)

    /**
     * Serialise Kotlin value to pretty string asynchronously.
     * @see Serialiser.toPrettyStringAsync
     */
    suspend fun toPrettyStringAsync(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): String = Serialiser.toPrettyStringAsync(value, config)

    /**
     * Serialise based on config.prettyPrint setting.
     * @see Serialiser.serialise
     */
    fun serialise(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String =
        Serialiser.serialise(value, config)

    /**
     * Serialise based on config.prettyPrint setting asynchronously.
     * @see Serialiser.serialiseAsync
     */
    suspend fun serialiseAsync(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String =
        Serialiser.serialiseAsync(value, config)

    // I/O functions (delegate to Io)

    /**
     * Read and parse GBLN file.
     * @see Io.readFile
     */
    fun readFile(path: String, config: GblnConfig = GblnConfig.DEFAULT): Any? =
        Io.readFile(path, config)

    /**
     * Write Kotlin value to GBLN file.
     * @see Io.writeFile
     */
    fun writeFile(path: String, value: Any?, config: GblnConfig = GblnConfig.DEFAULT) =
        Io.writeFile(path, value, config)

    /**
     * Read and parse GBLN file asynchronously.
     * @see Io.readFileAsync
     */
    suspend fun readFileAsync(path: String, config: GblnConfig = GblnConfig.DEFAULT): Any? =
        Io.readFileAsync(path, config)

    /**
     * Write Kotlin value to GBLN file asynchronously.
     * @see Io.writeFileAsync
     */
    suspend fun writeFileAsync(
        path: String,
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ) = Io.writeFileAsync(path, value, config)

    /**
     * Read GBLN file and cast to specific type.
     * @see Io.readFileAs
     */
    inline fun <reified T> readFileAs(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T = Io.readFileAs(path, config)

    /**
     * Read GBLN file asynchronously and cast to specific type.
     * @see Io.readFileAsAsync
     */
    suspend inline fun <reified T> readFileAsAsync(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T = Io.readFileAsAsync(path, config)
}
