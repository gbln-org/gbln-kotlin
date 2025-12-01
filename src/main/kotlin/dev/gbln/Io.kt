// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

import com.sun.jna.ptr.PointerByReference
import java.nio.file.Path

/**
 * Write GBLN value to I/O format file.
 *
 * This function serialises the value according to the configuration and writes
 * it to the specified file. The file extension and compression are determined
 * by the config settings.
 *
 * File Extensions:
 * - `.io.gbln.xz`: MINI GBLN + XZ compression (compress=true)
 * - `.io.gbln`: MINI GBLN without compression (compress=false, miniMode=true)
 * - `.gbln`: Pretty-printed source format (miniMode=false)
 *
 * @param value GBLN value to write
 * @param path File path (String or Path)
 * @param config I/O configuration (if null, uses default io format)
 * @throws IoError On file write failure
 * @throws IllegalArgumentException If value is null
 *
 * Example:
 * ```kotlin
 * val value = parse("user{id<u32>(12345)name<s64>(Alice)}")
 *
 * // Write with default I/O config (MINI + XZ compressed)
 * writeIo(value, "config.io.gbln.xz")
 *
 * // Write with custom config
 * val config = GblnConfig(compress = false)
 * writeIo(value, "config.io.gbln", config)
 * ```
 */
fun writeIo(value: ManagedGblnValue, path: String, config: GblnConfig? = null) {
    val pathBytes = path.toByteArray(Charsets.UTF_8)

    // Create C config
    val cConfig = if (config == null) {
        lib.gbln_config_new_io()
    } else {
        lib.gbln_config_new(
            config.miniMode,
            config.compress,
            config.compressionLevel,
            config.indent,
            config.stripComments
        )
    }

    try {
        // Call C FFI
        val err = lib.gbln_write_io(value.ptr, String(pathBytes), cConfig)

        if (err != GblnErrorCode.OK) {
            // Get error message
            val msgPtr = lib.gbln_last_error_message()
            val msg = if (msgPtr != null) {
                val errorMsg = msgPtr.getString(0, "UTF-8")
                lib.gbln_string_free(msgPtr)
                errorMsg
            } else {
                "I/O error (code $err)"
            }

            throw IoError(msg)
        }
    } finally {
        // Free C config
        lib.gbln_config_free(cConfig)
    }
}

/**
 * Write GBLN value to I/O format file (Path overload).
 */
fun writeIo(value: ManagedGblnValue, path: Path, config: GblnConfig? = null) {
    writeIo(value, path.toString(), config)
}

/**
 * Read GBLN file from I/O format (low-level API).
 *
 * Returns a ManagedGblnValue that can be passed to writeIo() or serialised.
 * Use readIo() instead if you want automatic conversion to Kotlin types.
 *
 * This function reads a file and automatically detects if it's XZ compressed.
 *
 * @param path File path (String or Path)
 * @return ManagedGblnValue with automatic memory cleanup
 * @throws IoError On file read failure
 * @throws ParseError On invalid GBLN content
 */
fun readIoRaw(path: String): ManagedGblnValue {
    val pathBytes = path.toByteArray(Charsets.UTF_8)

    // Prepare output pointer
    val outValue = PointerByReference()

    // Call C FFI
    val err = lib.gbln_read_io(String(pathBytes), outValue)

    if (err != GblnErrorCode.OK) {
        // Get error message
        val msgPtr = lib.gbln_last_error_message()
        val msg = if (msgPtr != null) {
            val errorMsg = msgPtr.getString(0, "UTF-8")
            lib.gbln_string_free(msgPtr)
            errorMsg
        } else {
            "I/O error (code $err)"
        }

        throw IoError(msg)
    }

    // Wrap in ManagedGblnValue for automatic cleanup
    return ManagedGblnValue(outValue.value)
}

/**
 * Read GBLN file from I/O format.
 *
 * This function reads a file and automatically detects if it's XZ compressed.
 * The content is then parsed into a Kotlin value.
 *
 * Auto-Detection:
 * The function checks for XZ magic bytes (FD 37 7A 58 5A 00) and automatically
 * decompresses if detected.
 *
 * @param path File path (String or Path)
 * @return Parsed Kotlin value (Map, List, or primitive)
 * @throws IoError On file read failure
 * @throws ParseError On invalid GBLN content
 *
 * Example:
 * ```kotlin
 * // Reads and auto-decompresses if .xz
 * val value = readIo("config.io.gbln.xz")
 *
 * // Also works with uncompressed files
 * val value2 = readIo("config.io.gbln")
 * ```
 */
fun readIo(path: String): Any? {
    val managedValue = readIoRaw(path)
    return gblnToKotlin(managedValue.ptr)
}

/**
 * Read GBLN file from I/O format (Path overload).
 */
fun readIo(path: Path): Any? {
    return readIo(path.toString())
}
