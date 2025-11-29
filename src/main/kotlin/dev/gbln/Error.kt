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
 * Sealed class hierarchy representing all possible GBLN errors.
 *
 * Provides type-safe error handling with specific error cases for each
 * failure mode in GBLN parsing, serialisation, and I/O operations.
 */
sealed class GblnError(message: String) : Exception(message) {

    /**
     * Parsing errors from invalid GBLN syntax or structure.
     */
    data class ParseError(val details: String) : GblnError("Parse error: $details")

    /**
     * Type validation errors when values don't match declared types.
     */
    data class TypeError(val details: String) : GblnError("Type error: $details")

    /**
     * Integer value out of range for declared type.
     */
    data class IntegerOutOfRange(
        val value: Long,
        val typeName: String,
        val min: Long,
        val max: Long
    ) : GblnError("Integer $value out of range for $typeName (valid range: $min to $max)")

    /**
     * String exceeds maximum length for declared type.
     */
    data class StringTooLong(
        val length: Int,
        val maxLength: Int,
        val value: String
    ) : GblnError("String length $length exceeds maximum $maxLength: \"${value.take(50)}...\"")

    /**
     * Floating-point value cannot be represented in declared precision.
     */
    data class FloatPrecisionLoss(
        val value: Double,
        val typeName: String
    ) : GblnError("Float value $value loses precision when converted to $typeName")

    /**
     * File I/O operation failed.
     */
    data class IoError(val details: String) : GblnError("I/O error: $details")

    /**
     * File does not exist or cannot be accessed.
     */
    data class FileNotFound(val path: String) : GblnError("File not found: $path")

    /**
     * Insufficient permissions to read or write file.
     */
    data class PermissionDenied(val path: String) : GblnError("Permission denied: $path")

    /**
     * Serialisation failed (value cannot be converted to GBLN).
     */
    data class SerialisationError(val details: String) : GblnError("Serialisation error: $details")

    /**
     * Null pointer or invalid memory reference in FFI layer.
     */
    data class NullPointer(val operation: String) : GblnError("Null pointer in operation: $operation")

    /**
     * Invalid type conversion attempted.
     */
    data class ConversionError(
        val fromType: String,
        val toType: String
    ) : GblnError("Cannot convert from $fromType to $toType")

    /**
     * Object key not found.
     */
    data class KeyNotFound(val key: String) : GblnError("Key not found: $key")

    /**
     * Array index out of bounds.
     */
    data class IndexOutOfBounds(
        val index: Int,
        val length: Int
    ) : GblnError("Index $index out of bounds for array of length $length")

    /**
     * Duplicate key in GBLN object.
     */
    data class DuplicateKey(val key: String) : GblnError("Duplicate key: $key")

    /**
     * Invalid UTF-8 sequence in string.
     */
    data class InvalidUtf8(val details: String) : GblnError("Invalid UTF-8: $details")

    /**
     * Generic error for cases not covered by specific error types.
     */
    data class GenericError(val details: String) : GblnError(details)
}

/**
 * Result type for operations that may fail with a GblnError.
 *
 * Provides Railway-Oriented Programming pattern for error handling.
 */
sealed class GblnResult<out T> {
    data class Success<T>(val value: T) : GblnResult<T>()
    data class Failure(val error: GblnError) : GblnResult<Nothing>()

    /**
     * Returns true if this is a Success result.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this is a Failure result.
     */
    fun isFailure(): Boolean = this is Failure

    /**
     * Returns the value if Success, or throws the error if Failure.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    /**
     * Returns the value if Success, or the default value if Failure.
     */
    fun getOrDefault(default: T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    /**
     * Maps the success value using the given transform function.
     */
    fun <R> map(transform: (T) -> R): GblnResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Flat-maps the success value using the given transform function.
     */
    fun <R> flatMap(transform: (T) -> GblnResult<R>): GblnResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    /**
     * Recovers from failure using the given recovery function.
     */
    fun recover(recovery: (GblnError) -> T): T = when (this) {
        is Success -> value
        is Failure -> recovery(error)
    }

    /**
     * Executes the given action if this is a Success.
     */
    fun onSuccess(action: (T) -> Unit): GblnResult<T> {
        if (this is Success) {
            action(value)
        }
        return this
    }

    /**
     * Executes the given action if this is a Failure.
     */
    fun onFailure(action: (GblnError) -> Unit): GblnResult<T> {
        if (this is Failure) {
            action(error)
        }
        return this
    }
}

/**
 * Extension function to convert nullable values to GblnResult.
 */
fun <T> T?.toResult(errorMessage: String): GblnResult<T> = when (this) {
    null -> GblnResult.Failure(GblnError.NullPointer(errorMessage))
    else -> GblnResult.Success(this)
}

/**
 * Extension function to wrap exceptions in GblnResult.
 */
inline fun <T> runCatchingGbln(block: () -> T): GblnResult<T> = try {
    GblnResult.Success(block())
} catch (e: GblnError) {
    GblnResult.Failure(e)
} catch (e: Exception) {
    GblnResult.Failure(GblnError.GenericError(e.message ?: "Unknown error"))
}
