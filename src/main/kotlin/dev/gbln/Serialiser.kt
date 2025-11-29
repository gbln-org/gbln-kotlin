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
 * GBLN serialiser providing both synchronous and asynchronous serialisation.
 *
 * Converts Kotlin values to GBLN text format with configurable formatting.
 */
object Serialiser {

    /**
     * Serialise Kotlin value to compact GBLN string.
     *
     * Produces minimal output without whitespace or formatting.
     * Ideal for LLM contexts and network transmission.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return GBLN string representation
     * @throws GblnError if serialisation fails
     */
    fun toString(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String {
        return ValueConversion.toGbln(value).use { managed ->
            val result = FfiWrapper.toString(managed.pointer())
            result ?: throw GblnError.SerialisationError("Failed to serialise value")
        }
    }

    /**
     * Serialise Kotlin value to pretty-printed GBLN string.
     *
     * Produces formatted output with indentation and newlines for readability.
     * Ideal for configuration files and human-readable output.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return Pretty-printed GBLN string representation
     * @throws GblnError if serialisation fails
     */
    fun toPrettyString(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String {
        return ValueConversion.toGbln(value).use { managed ->
            val result = FfiWrapper.toPrettyString(managed.pointer())
            result ?: throw GblnError.SerialisationError("Failed to serialise value")
        }
    }

    /**
     * Serialise Kotlin value to GBLN string asynchronously.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return GBLN string representation
     * @throws GblnError if serialisation fails
     */
    suspend fun toStringAsync(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): String {
        return withContext(Dispatchers.IO) {
            toString(value, config)
        }
    }

    /**
     * Serialise Kotlin value to pretty-printed GBLN string asynchronously.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return Pretty-printed GBLN string representation
     * @throws GblnError if serialisation fails
     */
    suspend fun toPrettyStringAsync(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): String {
        return withContext(Dispatchers.IO) {
            toPrettyString(value, config)
        }
    }

    /**
     * Serialise Kotlin value and return Result type.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return GblnResult.Success with string, or GblnResult.Failure with error
     */
    fun toStringResult(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): GblnResult<String> {
        return runCatchingGbln {
            toString(value, config)
        }
    }

    /**
     * Serialise Kotlin value to pretty string and return Result type.
     *
     * @param value The Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return GblnResult.Success with string, or GblnResult.Failure with error
     */
    fun toPrettyStringResult(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): GblnResult<String> {
        return runCatchingGbln {
            toPrettyString(value, config)
        }
    }

    /**
     * Serialise based on config.prettyPrint setting.
     *
     * @param value The Kotlin value to serialise
     * @param config Configuration determining output format
     * @return GBLN string representation (compact or pretty)
     * @throws GblnError if serialisation fails
     */
    fun serialise(value: Any?, config: GblnConfig = GblnConfig.DEFAULT): String {
        return if (config.prettyPrint) {
            toPrettyString(value, config)
        } else {
            toString(value, config)
        }
    }

    /**
     * Serialise based on config.prettyPrint setting asynchronously.
     *
     * @param value The Kotlin value to serialise
     * @param config Configuration determining output format
     * @return GBLN string representation (compact or pretty)
     * @throws GblnError if serialisation fails
     */
    suspend fun serialiseAsync(
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): String {
        return if (config.prettyPrint) {
            toPrettyStringAsync(value, config)
        } else {
            toStringAsync(value, config)
        }
    }
}

/**
 * Extension function to convert any Kotlin value to compact GBLN string.
 */
fun Any?.toGblnString(config: GblnConfig = GblnConfig.DEFAULT): String {
    return Serialiser.toString(this, config)
}

/**
 * Extension function to convert any Kotlin value to pretty GBLN string.
 */
fun Any?.toGblnPrettyString(config: GblnConfig = GblnConfig.DEFAULT): String {
    return Serialiser.toPrettyString(this, config)
}

/**
 * Extension function to convert any Kotlin value to GBLN string asynchronously.
 */
suspend fun Any?.toGblnStringAsync(config: GblnConfig = GblnConfig.DEFAULT): String {
    return Serialiser.toStringAsync(this, config)
}

/**
 * Extension function to convert any Kotlin value to pretty GBLN string asynchronously.
 */
suspend fun Any?.toGblnPrettyStringAsync(config: GblnConfig = GblnConfig.DEFAULT): String {
    return Serialiser.toPrettyStringAsync(this, config)
}
