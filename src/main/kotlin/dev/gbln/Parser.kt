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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * GBLN parser providing both synchronous and asynchronous parsing functions.
 *
 * Parses GBLN text format into Kotlin values with full type validation.
 */
object Parser {

    /**
     * Parse GBLN string synchronously.
     *
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return Parsed Kotlin value (Map, List, or primitive)
     * @throws GblnError if parsing fails
     */
    fun parse(input: String, config: GblnConfig = GblnConfig.DEFAULT): Any? {
        val ptr = FfiWrapper.parse(input)

        if (ptr == 0L) {
            val errorMsg = FfiWrapper.getErrorMessage() ?: "Unknown parse error"
            throw GblnError.ParseError(errorMsg)
        }

        return ManagedValue(ptr).use { managed ->
            ValueConversion.fromGbln(managed.pointer())
        }
    }

    /**
     * Parse GBLN string asynchronously using coroutines.
     *
     * Performs parsing on the IO dispatcher to avoid blocking the calling thread.
     *
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return Parsed Kotlin value (Map, List, or primitive)
     * @throws GblnError if parsing fails
     */
    suspend fun parseAsync(input: String, config: GblnConfig = GblnConfig.DEFAULT): Any? {
        return withContext(Dispatchers.IO) {
            parse(input, config)
        }
    }

    /**
     * Parse GBLN string and return Result type.
     *
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return GblnResult.Success with value, or GblnResult.Failure with error
     */
    fun parseResult(input: String, config: GblnConfig = GblnConfig.DEFAULT): GblnResult<Any?> {
        return runCatchingGbln {
            parse(input, config)
        }
    }

    /**
     * Parse GBLN string asynchronously and return Result type.
     *
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return GblnResult.Success with value, or GblnResult.Failure with error
     */
    suspend fun parseResultAsync(
        input: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): GblnResult<Any?> {
        return runCatchingGbln {
            parseAsync(input, config)
        }
    }

    /**
     * Parse GBLN string and cast to specific type.
     *
     * @param T The expected type of the parsed value
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return Parsed value cast to type T
     * @throws GblnError if parsing fails or type cast fails
     */
    inline fun <reified T> parseAs(
        input: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T {
        val value = parse(input, config)
        return value as? T ?: throw GblnError.ConversionError(
            value?.let { it::class.simpleName } ?: "null",
            T::class.simpleName ?: "Unknown"
        )
    }

    /**
     * Parse GBLN string asynchronously and cast to specific type.
     *
     * @param T The expected type of the parsed value
     * @param input The GBLN string to parse
     * @param config Optional configuration for parsing behaviour
     * @return Parsed value cast to type T
     * @throws GblnError if parsing fails or type cast fails
     */
    suspend inline fun <reified T> parseAsAsync(
        input: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T {
        val value = parseAsync(input, config)
        return value as? T ?: throw GblnError.ConversionError(
            value?.let { it::class.simpleName } ?: "null",
            T::class.simpleName ?: "Unknown"
        )
    }
}

/**
 * Extension function to parse a String as GBLN.
 */
fun String.parseGbln(config: GblnConfig = GblnConfig.DEFAULT): Any? {
    return Parser.parse(this, config)
}

/**
 * Extension function to parse a String as GBLN asynchronously.
 */
suspend fun String.parseGblnAsync(config: GblnConfig = GblnConfig.DEFAULT): Any? {
    return Parser.parseAsync(this, config)
}

/**
 * Extension function to parse a String as GBLN and cast to type T.
 */
inline fun <reified T> String.parseGblnAs(config: GblnConfig = GblnConfig.DEFAULT): T {
    return Parser.parseAs(this, config)
}

/**
 * Extension function to parse a String as GBLN asynchronously and cast to type T.
 */
suspend inline fun <reified T> String.parseGblnAsAsync(
    config: GblnConfig = GblnConfig.DEFAULT
): T {
    return Parser.parseAsAsync(this, config)
}
