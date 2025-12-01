// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

/**
 * Configuration for GBLN I/O operations.
 *
 * Controls serialisation format, compression, and output style.
 *
 * @property miniMode Use MINI GBLN format (no whitespace). Default: true
 * @property compress Enable XZ compression. Default: true
 * @property compressionLevel XZ compression level (0-9). Default: 6
 * @property indent Indentation width for pretty format. Default: 2
 * @property stripComments Remove comments in I/O files. Default: true
 *
 * @throws IllegalArgumentException if compressionLevel not in 0-9 or indent < 0
 *
 * Example:
 * ```kotlin
 * // No compression
 * val config = GblnConfig(compress = false)
 * writeIo(data, "output.io.gbln", config)
 *
 * // Pretty-printed with 4-space indentation
 * val prettyConfig = GblnConfig(miniMode = false, indent = 4)
 * writeIo(data, "formatted.gbln", prettyConfig)
 * ```
 */
data class GblnConfig(
    val miniMode: Boolean = true,
    val compress: Boolean = true,
    val compressionLevel: Int = 6,
    val indent: Int = 2,
    val stripComments: Boolean = true
) {
    init {
        require(compressionLevel in 0..9) {
            "compressionLevel must be 0-9, got $compressionLevel"
        }
        require(indent >= 0) {
            "indent must be >= 0, got $indent"
        }
    }
}
