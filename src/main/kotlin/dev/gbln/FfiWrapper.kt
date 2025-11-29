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

import java.io.File

/**
 * Low-level FFI wrapper for GBLN native library via JNI.
 *
 * This class provides direct access to the native GBLN functions through
 * JNI. All methods are internal and should not be used directly by clients.
 * Use the high-level API in Gbln.kt instead.
 *
 * Memory management: Pointers returned from native functions must be freed
 * using the appropriate free function to prevent memory leaks.
 */
internal object FfiWrapper {

    init {
        // Detect platform and load appropriate library
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()

        val platform = when {
            osName.contains("mac") && osArch.contains("aarch64") -> "macos-arm64"
            osName.contains("mac") && osArch.contains("x86_64") -> "macos-x64"
            osName.contains("linux") && osArch.contains("aarch64") -> "linux-arm64"
            osName.contains("linux") && osArch.contains("x86_64") -> "linux-x64"
            osName.contains("windows") && osArch.contains("aarch64") -> "windows-arm64"
            osName.contains("windows") && osArch.contains("amd64") -> "windows-x64"
            else -> throw UnsupportedOperationException("Unsupported platform: $osName $osArch")
        }

        // Load the native JNI library
        val libraryName = when {
            osName.contains("windows") -> "gbln_jni.dll"
            osName.contains("mac") -> "libgbln_jni.dylib"
            else -> "libgbln_jni.so"
        }

        // Try to load from system library path first
        try {
            System.loadLibrary("gbln_jni")
        } catch (e: UnsatisfiedLinkError) {
            // Fall back to loading from local build directory
            val projectRoot = File("").absolutePath
            val libraryPath = "$projectRoot/build/libs/$platform/$libraryName"
            System.load(libraryPath)
        }
    }

    // Error codes (must match GblnErrorCode enum from C FFI)
    object ErrorCode {
        const val OK = 0
        const val PARSE_ERROR = 1
        const val TYPE_MISMATCH = 2
        const val OUT_OF_RANGE = 3
        const val INVALID_UTF8 = 4
        const val NULL_POINTER = 5
        const val KEY_NOT_FOUND = 6
        const val INDEX_OUT_OF_BOUNDS = 7
        const val MEMORY_ERROR = 8
    }

    // Value types (must match GblnValueType enum from C FFI)
    object ValueType {
        const val I8 = 0
        const val I16 = 1
        const val I32 = 2
        const val I64 = 3
        const val U8 = 4
        const val U16 = 5
        const val U32 = 6
        const val U64 = 7
        const val F32 = 8
        const val F64 = 9
        const val BOOL = 10
        const val STRING = 11
        const val NULL = 12
        const val OBJECT = 13
        const val ARRAY = 14
    }

    // Parser functions

    /**
     * Parse GBLN string into a value pointer.
     * Returns error code. On success (0), outValue[0] contains the pointer.
     */
    external fun gblnParse(input: String, outValue: LongArray): Int

    /**
     * Parse GBLN file into a value pointer.
     * Returns error code. On success (0), outValue[0] contains the pointer.
     */
    external fun gblnParseFile(path: String, outValue: LongArray): Int

    // Serialiser functions

    /**
     * Convert value to compact GBLN string.
     * Returns Java string directly (memory managed by JNI).
     */
    external fun gblnToString(valuePtr: Long): String?

    /**
     * Convert value to human-readable GBLN string with formatting.
     * Returns Java string directly (memory managed by JNI).
     */
    external fun gblnToStringPretty(valuePtr: Long): String?

    /**
     * Write value to GBLN file.
     * Returns error code (0 = success).
     */
    external fun gblnWriteFile(path: String, valuePtr: Long): Int

    // Value type query

    /**
     * Get the type of a value.
     * Returns ValueType constant.
     */
    external fun gblnValueType(valuePtr: Long): Int

    // Value getter functions (with ok flag)

    external fun gblnValueAsI8(valuePtr: Long, ok: BooleanArray): Byte
    external fun gblnValueAsI16(valuePtr: Long, ok: BooleanArray): Short
    external fun gblnValueAsI32(valuePtr: Long, ok: BooleanArray): Int
    external fun gblnValueAsI64(valuePtr: Long, ok: BooleanArray): Long
    external fun gblnValueAsU8(valuePtr: Long, ok: BooleanArray): Short
    external fun gblnValueAsU16(valuePtr: Long, ok: BooleanArray): Int
    external fun gblnValueAsU32(valuePtr: Long, ok: BooleanArray): Long
    external fun gblnValueAsU64(valuePtr: Long, ok: BooleanArray): Long
    external fun gblnValueAsF32(valuePtr: Long, ok: BooleanArray): Float
    external fun gblnValueAsF64(valuePtr: Long, ok: BooleanArray): Double
    external fun gblnValueAsBool(valuePtr: Long, ok: BooleanArray): Boolean

    /**
     * Get string value from a value pointer.
     * Returns Java string directly (memory managed by JNI).
     * Sets ok[0] to true if successful, false otherwise.
     */
    external fun gblnValueAsString(valuePtr: Long, ok: BooleanArray): String?

    // Object operations

    /**
     * Get value from object by key.
     * Returns pointer to value (owned by object, do not free).
     * Returns 0 if key not found.
     */
    external fun gblnObjectGet(objectPtr: Long, key: String): Long

    /**
     * Set key-value pair in object.
     * The value pointer is consumed by the object (do not free separately).
     * Returns error code (0 = success).
     */
    external fun gblnObjectSet(objectPtr: Long, key: String, valuePtr: Long): Int

    /**
     * Get number of key-value pairs in object.
     */
    external fun gblnObjectLen(objectPtr: Long): Int

    /**
     * Get all keys in object.
     * Returns array of key strings.
     */
    external fun gblnObjectKeys(objectPtr: Long): Array<String>

    // Array operations

    /**
     * Get value from array by index.
     * Returns pointer to value (owned by array, do not free).
     * Returns 0 if index out of bounds.
     */
    external fun gblnArrayGet(arrayPtr: Long, index: Int): Long

    /**
     * Push value to end of array.
     * The value pointer is consumed by the array (do not free separately).
     * Returns error code (0 = success).
     */
    external fun gblnArrayPush(arrayPtr: Long, valuePtr: Long): Int

    /**
     * Get number of elements in array.
     */
    external fun gblnArrayLen(arrayPtr: Long): Int

    // Value creation functions

    external fun gblnValueNewI8(value: Byte): Long
    external fun gblnValueNewI16(value: Short): Long
    external fun gblnValueNewI32(value: Int): Long
    external fun gblnValueNewI64(value: Long): Long
    external fun gblnValueNewU8(value: Short): Long
    external fun gblnValueNewU16(value: Int): Long
    external fun gblnValueNewU32(value: Long): Long
    external fun gblnValueNewU64(value: Long): Long
    external fun gblnValueNewF32(value: Float): Long
    external fun gblnValueNewF64(value: Double): Long
    external fun gblnValueNewBool(value: Boolean): Long
    external fun gblnValueNewString(value: String, maxLen: Int): Long
    external fun gblnValueNewNull(): Long
    external fun gblnValueNewObject(): Long
    external fun gblnValueNewArray(): Long

    // Memory management

    /**
     * Free a value pointer and all its contents.
     * Must be called exactly once for each created/parsed value.
     */
    external fun gblnValueFree(valuePtr: Long)

    /**
     * Free a string pointer returned from serialisation or gblnValueAsString.
     */
    external fun gblnStringFree(stringPtr: Long)

    /**
     * Get error message for an error code.
     * Returns human-readable error description.
     */
    external fun gblnErrorMessage(errorCode: Int): String
}

/**
 * Helper functions for working with the FFI layer.
 */
internal object FfiHelpers {

    /**
     * Parse GBLN string and return value pointer.
     * Throws GblnError on failure.
     */
    fun parse(input: String): Long {
        val outValue = LongArray(1)
        val errorCode = FfiWrapper.gblnParse(input, outValue)

        if (errorCode != FfiWrapper.ErrorCode.OK) {
            val errorMsg = FfiWrapper.gblnErrorMessage(errorCode)
            throw GblnError.ParseError(errorMsg)
        }

        return outValue[0]
    }

    /**
     * Parse GBLN file and return value pointer.
     * Throws GblnError on failure.
     */
    fun parseFile(path: String): Long {
        val outValue = LongArray(1)
        val errorCode = FfiWrapper.gblnParseFile(path, outValue)

        if (errorCode != FfiWrapper.ErrorCode.OK) {
            val errorMsg = FfiWrapper.gblnErrorMessage(errorCode)
            throw GblnError.ParseError("Failed to parse file '$path': $errorMsg")
        }

        return outValue[0]
    }

    /**
     * Serialise value to GBLN string.
     * Returns Kotlin String (memory managed by JNI).
     */
    fun serialise(valuePtr: Long): String {
        val result = FfiWrapper.gblnToString(valuePtr)
        return result ?: throw GblnError.SerialiseError("Failed to serialise value")
    }

    /**
     * Serialise value to pretty GBLN string.
     * Returns Kotlin String (memory managed by JNI).
     */
    fun serialisePretty(valuePtr: Long): String {
        val result = FfiWrapper.gblnToStringPretty(valuePtr)
        return result ?: throw GblnError.SerialiseError("Failed to serialise value to pretty format")
    }

    /**
     * Get string value from a value pointer.
     * Throws GblnError if not a string type.
     */
    fun getString(valuePtr: Long): String {
        val ok = BooleanArray(1)
        val result = FfiWrapper.gblnValueAsString(valuePtr, ok)

        if (!ok[0] || result == null) {
            throw GblnError.TypeError("Failed to get string value")
        }

        return result
    }

    /**
     * Check if value type matches expected type.
     * Throws TypeError if mismatch.
     */
    fun checkType(valuePtr: Long, expectedType: Int) {
        val actualType = FfiWrapper.gblnValueType(valuePtr)
        if (actualType != expectedType) {
            throw GblnError.TypeError(
                "Expected type ${typeNameFor(expectedType)}, got ${typeNameFor(actualType)}"
            )
        }
    }

    /**
     * Get human-readable name for value type constant.
     */
    private fun typeNameFor(typeCode: Int): String = when (typeCode) {
        FfiWrapper.ValueType.I8 -> "i8"
        FfiWrapper.ValueType.I16 -> "i16"
        FfiWrapper.ValueType.I32 -> "i32"
        FfiWrapper.ValueType.I64 -> "i64"
        FfiWrapper.ValueType.U8 -> "u8"
        FfiWrapper.ValueType.U16 -> "u16"
        FfiWrapper.ValueType.U32 -> "u32"
        FfiWrapper.ValueType.U64 -> "u64"
        FfiWrapper.ValueType.F32 -> "f32"
        FfiWrapper.ValueType.F64 -> "f64"
        FfiWrapper.ValueType.BOOL -> "bool"
        FfiWrapper.ValueType.STRING -> "string"
        FfiWrapper.ValueType.NULL -> "null"
        FfiWrapper.ValueType.OBJECT -> "object"
        FfiWrapper.ValueType.ARRAY -> "array"
        else -> "unknown($typeCode)"
    }
}

/**
 * RAII wrapper for native value pointers.
 *
 * Automatically frees the native pointer when the object is closed.
 * This prevents memory leaks from forgotten gblnValueFree() calls.
 */
internal class ManagedValue(private val ptr: Long) : AutoCloseable {

    /**
     * Check if the pointer is valid (non-zero).
     */
    fun isValid(): Boolean = ptr != 0L

    /**
     * Get the raw pointer value.
     */
    fun pointer(): Long = ptr

    /**
     * Manually free the native pointer.
     * After calling close(), this object should not be used.
     */
    override fun close() {
        if (ptr != 0L) {
            FfiWrapper.gblnValueFree(ptr)
        }
    }

    /**
     * Ensure the native pointer is freed when garbage collected.
     */
    protected fun finalize() {
        close()
    }
}

/**
 * Extension function to execute a block with a managed value.
 * Automatically frees the value when the block completes.
 */
internal inline fun <T> Long.useManagedValue(block: (Long) -> T): T {
    return ManagedValue(this).use { managed ->
        if (!managed.isValid()) {
            throw GblnError.NullPointer("Invalid value pointer")
        }
        block(managed.pointer())
    }
}
