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
 * Configuration options for GBLN parsing and serialisation.
 *
 * Controls formatting, validation strictness, and performance trade-offs.
 */
data class GblnConfig(
    /**
     * Enable strict type validation during parsing.
     * When true, type mismatches and range violations throw errors.
     * When false, the parser attempts best-effort conversion.
     * Default: true
     */
    val strictTypes: Boolean = true,

    /**
     * Enable pretty-printing in serialisation.
     * When true, output includes indentation and newlines for readability.
     * When false, output is compact for minimal size.
     * Default: false
     */
    val prettyPrint: Boolean = false,

    /**
     * Indentation string for pretty-printed output.
     * Only used when prettyPrint is true.
     * Default: 4 spaces
     */
    val indent: String = "    ",

    /**
     * Strip comments during parsing.
     * When true, comments (:| ...) are removed from parsed values.
     * When false, comments are preserved in string representations.
     * Default: false
     */
    val stripComments: Boolean = false,

    /**
     * Validate duplicate keys in objects.
     * When true, duplicate keys in objects cause parse errors.
     * When false, duplicate keys overwrite previous values.
     * Default: true
     */
    val checkDuplicateKeys: Boolean = true,

    /**
     * Maximum nesting depth for objects and arrays.
     * Prevents stack overflow from deeply nested structures.
     * Set to 0 for unlimited depth (not recommended).
     * Default: 256
     */
    val maxDepth: Int = 256,

    /**
     * Maximum string length for auto-type selection.
     * When converting strings without explicit type hints, this determines
     * the upper limit for automatic type selection.
     * Default: 1024 (s1024)
     */
    val maxAutoStringLength: Int = 1024
) {

    init {
        require(indent.isNotEmpty()) { "Indent string cannot be empty" }
        require(maxDepth >= 0) { "Max depth cannot be negative" }
        require(maxAutoStringLength in listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)) {
            "Max auto string length must be a valid GBLN string size"
        }
    }

    companion object {
        /**
         * Default configuration with sensible defaults.
         */
        val DEFAULT = GblnConfig()

        /**
         * Configuration optimised for LLM contexts.
         * - Compact output (no pretty-print)
         * - Comments stripped
         * - Strict validation enabled
         */
        val LLM_OPTIMISED = GblnConfig(
            strictTypes = true,
            prettyPrint = false,
            stripComments = true,
            checkDuplicateKeys = true,
            maxDepth = 256,
            maxAutoStringLength = 1024
        )

        /**
         * Configuration for human-readable output.
         * - Pretty-print enabled
         * - Comments preserved
         * - Relaxed validation
         */
        val HUMAN_READABLE = GblnConfig(
            strictTypes = false,
            prettyPrint = true,
            stripComments = false,
            checkDuplicateKeys = false,
            maxDepth = 256,
            maxAutoStringLength = 1024
        )

        /**
         * Configuration for development and debugging.
         * - Pretty-print enabled
         * - Strict validation
         * - Comments preserved
         * - All checks enabled
         */
        val DEBUG = GblnConfig(
            strictTypes = true,
            prettyPrint = true,
            stripComments = false,
            checkDuplicateKeys = true,
            maxDepth = 256,
            maxAutoStringLength = 1024
        )
    }
}
