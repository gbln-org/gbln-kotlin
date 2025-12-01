// Copyright (c) 2025 Vivian Burkhard Voss
// SPDX-License-Identifier: Apache-2.0

package dev.gbln

/**
 * Base exception for all GBLN errors.
 *
 * All GBLN-specific exceptions inherit from this class,
 * allowing for easy catching of all GBLN-related errors.
 */
sealed class GblnError(message: String) : Exception(message)

/**
 * Raised when parsing fails.
 *
 * Examples:
 * - Syntax errors in GBLN input
 * - Unexpected characters
 * - Unterminated strings
 * - Invalid type hints
 */
class ParseError(message: String) : GblnError(message)

/**
 * Raised when validation fails.
 *
 * Examples:
 * - Integer out of range for type
 * - String exceeds maximum length
 * - Type mismatch
 * - Duplicate keys in object
 */
class ValidationError(message: String) : GblnError(message)

/**
 * Raised when I/O operations fail.
 *
 * Examples:
 * - File not found
 * - Permission denied
 * - Compression/decompression errors
 * - Invalid file format
 */
class IoError(message: String) : GblnError(message)

/**
 * Raised when serialisation fails.
 *
 * Examples:
 * - Unsupported Kotlin type
 * - Value exceeds type limits
 * - Invalid configuration
 */
class SerialiseError(message: String) : GblnError(message)
