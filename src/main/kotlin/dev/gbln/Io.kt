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
import java.io.File
import java.nio.file.Path

/**
 * I/O operations for reading and writing GBLN files.
 *
 * Provides both synchronous and asynchronous file operations with proper
 * error handling for file system errors.
 */
object Io {

    /**
     * Read and parse GBLN file synchronously.
     *
     * @param path Path to the GBLN file
     * @param config Optional configuration for parsing behaviour
     * @return Parsed Kotlin value
     * @throws GblnError.FileNotFound if file doesn't exist
     * @throws GblnError.PermissionDenied if file isn't readable
     * @throws GblnError.ParseError if parsing fails
     */
    fun readFile(path: String, config: GblnConfig = GblnConfig.DEFAULT): Any? {
        val file = File(path)

        if (!file.exists()) {
            throw GblnError.FileNotFound(path)
        }

        if (!file.canRead()) {
            throw GblnError.PermissionDenied(path)
        }

        val ptr = FfiWrapper.parseFile(path)

        if (ptr == 0L) {
            val errorMsg = FfiWrapper.getErrorMessage() ?: "Failed to read file"
            throw GblnError.IoError(errorMsg)
        }

        return ManagedValue(ptr).use { managed ->
            ValueConversion.fromGbln(managed.pointer())
        }
    }

    /**
     * Write Kotlin value to GBLN file synchronously.
     *
     * @param path Path to write the GBLN file
     * @param value Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @throws GblnError.PermissionDenied if file isn't writable
     * @throws GblnError.IoError if writing fails
     */
    fun writeFile(
        path: String,
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ) {
        val file = File(path)

        // Check parent directory exists and is writable
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw GblnError.IoError("Failed to create directory: ${parent.absolutePath}")
            }
        }

        if (file.exists() && !file.canWrite()) {
            throw GblnError.PermissionDenied(path)
        }

        ValueConversion.toGbln(value).use { managed ->
            val result = FfiWrapper.writeFile(path, managed.pointer())

            if (result != 0) {
                val errorMsg = FfiWrapper.getErrorMessage() ?: "Failed to write file"
                throw GblnError.IoError(errorMsg)
            }
        }
    }

    /**
     * Read and parse GBLN file asynchronously.
     *
     * @param path Path to the GBLN file
     * @param config Optional configuration for parsing behaviour
     * @return Parsed Kotlin value
     * @throws GblnError.FileNotFound if file doesn't exist
     * @throws GblnError.PermissionDenied if file isn't readable
     * @throws GblnError.ParseError if parsing fails
     */
    suspend fun readFileAsync(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): Any? {
        return withContext(Dispatchers.IO) {
            readFile(path, config)
        }
    }

    /**
     * Write Kotlin value to GBLN file asynchronously.
     *
     * @param path Path to write the GBLN file
     * @param value Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @throws GblnError.PermissionDenied if file isn't writable
     * @throws GblnError.IoError if writing fails
     */
    suspend fun writeFileAsync(
        path: String,
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ) {
        withContext(Dispatchers.IO) {
            writeFile(path, value, config)
        }
    }

    /**
     * Read GBLN file and return Result type.
     *
     * @param path Path to the GBLN file
     * @param config Optional configuration for parsing behaviour
     * @return GblnResult.Success with value, or GblnResult.Failure with error
     */
    fun readFileResult(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): GblnResult<Any?> {
        return runCatchingGbln {
            readFile(path, config)
        }
    }

    /**
     * Write GBLN file and return Result type.
     *
     * @param path Path to write the GBLN file
     * @param value Kotlin value to serialise
     * @param config Optional configuration for serialisation behaviour
     * @return GblnResult.Success with Unit, or GblnResult.Failure with error
     */
    fun writeFileResult(
        path: String,
        value: Any?,
        config: GblnConfig = GblnConfig.DEFAULT
    ): GblnResult<Unit> {
        return runCatchingGbln {
            writeFile(path, value, config)
        }
    }

    /**
     * Read GBLN file and cast to specific type.
     *
     * @param T Expected type of the parsed value
     * @param path Path to the GBLN file
     * @param config Optional configuration for parsing behaviour
     * @return Parsed value cast to type T
     * @throws GblnError if reading fails or type cast fails
     */
    inline fun <reified T> readFileAs(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T {
        val value = readFile(path, config)
        return value as? T ?: throw GblnError.ConversionError(
            value?.let { it::class.simpleName } ?: "null",
            T::class.simpleName ?: "Unknown"
        )
    }

    /**
     * Read GBLN file asynchronously and cast to specific type.
     *
     * @param T Expected type of the parsed value
     * @param path Path to the GBLN file
     * @param config Optional configuration for parsing behaviour
     * @return Parsed value cast to type T
     * @throws GblnError if reading fails or type cast fails
     */
    suspend inline fun <reified T> readFileAsAsync(
        path: String,
        config: GblnConfig = GblnConfig.DEFAULT
    ): T {
        val value = readFileAsync(path, config)
        return value as? T ?: throw GblnError.ConversionError(
            value?.let { it::class.simpleName } ?: "null",
            T::class.simpleName ?: "Unknown"
        )
    }
}

/**
 * Extension function to read GBLN file from File object.
 */
fun File.readGbln(config: GblnConfig = GblnConfig.DEFAULT): Any? {
    return Io.readFile(this.absolutePath, config)
}

/**
 * Extension function to write GBLN file to File object.
 */
fun File.writeGbln(value: Any?, config: GblnConfig = GblnConfig.DEFAULT) {
    Io.writeFile(this.absolutePath, value, config)
}

/**
 * Extension function to read GBLN file from Path object.
 */
fun Path.readGbln(config: GblnConfig = GblnConfig.DEFAULT): Any? {
    return Io.readFile(this.toString(), config)
}

/**
 * Extension function to write GBLN file to Path object.
 */
fun Path.writeGbln(value: Any?, config: GblnConfig = GblnConfig.DEFAULT) {
    Io.writeFile(this.toString(), value, config)
}

/**
 * Extension function to read GBLN file asynchronously from File object.
 */
suspend fun File.readGblnAsync(config: GblnConfig = GblnConfig.DEFAULT): Any? {
    return Io.readFileAsync(this.absolutePath, config)
}

/**
 * Extension function to write GBLN file asynchronously to File object.
 */
suspend fun File.writeGblnAsync(value: Any?, config: GblnConfig = GblnConfig.DEFAULT) {
    Io.writeFileAsync(this.absolutePath, value, config)
}
