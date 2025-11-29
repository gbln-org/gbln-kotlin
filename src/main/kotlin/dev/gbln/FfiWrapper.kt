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
        // Load the native library
        System.loadLibrary("gbln_jni")
    }

    // Parser functions

    /**
     * Parse GBLN string into a value pointer.
     * Returns 0 if parsing fails. Call getErrorMessage() for details.
     */
    external fun parse(input: String): Long

    // Serialiser functions

    /**
     * Convert value to compact GBLN string.
     * Caller must free the returned string using stringFree().
     */
    external fun toString(valuePtr: Long): String?

    /**
     * Convert value to human-readable GBLN string with formatting.
     * Caller must free the returned string using stringFree().
     */
    external fun toPrettyString(valuePtr: Long): String?

    // Value creation functions

    external fun valueNewI8(value: Byte): Long
    external fun valueNewI16(value: Short): Long
    external fun valueNewI32(value: Int): Long
    external fun valueNewI64(value: Long): Long
    external fun valueNewU8(value: Short): Long
    external fun valueNewU16(value: Int): Long
    external fun valueNewU32(value: Long): Long
    external fun valueNewU64(value: Long): Long
    external fun valueNewF32(value: Float): Long
    external fun valueNewF64(value: Double): Long
    external fun valueNewBool(value: Boolean): Long
    external fun valueNewString(value: String, maxLen: Int): Long
    external fun valueNewNull(): Long
    external fun valueNewObject(): Long
    external fun valueNewArray(): Long

    // Value type checking functions

    external fun valueIsI8(valuePtr: Long): Boolean
    external fun valueIsI16(valuePtr: Long): Boolean
    external fun valueIsI32(valuePtr: Long): Boolean
    external fun valueIsI64(valuePtr: Long): Boolean
    external fun valueIsU8(valuePtr: Long): Boolean
    external fun valueIsU16(valuePtr: Long): Boolean
    external fun valueIsU32(valuePtr: Long): Boolean
    external fun valueIsU64(valuePtr: Long): Boolean
    external fun valueIsF32(valuePtr: Long): Boolean
    external fun valueIsF64(valuePtr: Long): Boolean
    external fun valueIsBool(valuePtr: Long): Boolean
    external fun valueIsString(valuePtr: Long): Boolean
    external fun valueIsNull(valuePtr: Long): Boolean
    external fun valueIsObject(valuePtr: Long): Boolean
    external fun valueIsArray(valuePtr: Long): Boolean

    // Value getter functions

    external fun valueAsI8(valuePtr: Long): Byte
    external fun valueAsI16(valuePtr: Long): Short
    external fun valueAsI32(valuePtr: Long): Int
    external fun valueAsI64(valuePtr: Long): Long
    external fun valueAsU8(valuePtr: Long): Short
    external fun valueAsU16(valuePtr: Long): Int
    external fun valueAsU32(valuePtr: Long): Long
    external fun valueAsU64(valuePtr: Long): Long
    external fun valueAsF32(valuePtr: Long): Float
    external fun valueAsF64(valuePtr: Long): Double
    external fun valueAsBool(valuePtr: Long): Boolean
    external fun valueAsString(valuePtr: Long): String?

    // Object operations

    /**
     * Set key-value pair in object.
     * The value pointer is consumed by the object (do not free separately).
     */
    external fun objectSet(objectPtr: Long, key: String, valuePtr: Long)

    /**
     * Get value from object by key.
     * Returns 0 if key not found.
     * Returned pointer is owned by the object (do not free).
     */
    external fun objectGet(objectPtr: Long, key: String): Long

    /**
     * Get number of key-value pairs in object.
     */
    external fun objectLen(objectPtr: Long): Int

    /**
     * Get all keys in object.
     */
    external fun objectKeys(objectPtr: Long): Array<String>

    // Array operations

    /**
     * Push value to end of array.
     * The value pointer is consumed by the array (do not free separately).
     */
    external fun arrayPush(arrayPtr: Long, valuePtr: Long)

    /**
     * Get value from array by index.
     * Returns 0 if index out of bounds.
     * Returned pointer is owned by the array (do not free).
     */
    external fun arrayGet(arrayPtr: Long, index: Int): Long

    /**
     * Get number of elements in array.
     */
    external fun arrayLen(arrayPtr: Long): Int

    // Memory management

    /**
     * Free a value pointer and all its contents.
     * Must be called exactly once for each created/parsed value.
     */
    external fun valueFree(valuePtr: Long)

    /**
     * Free a string pointer returned from toString/toPrettyString.
     */
    external fun stringFree(stringPtr: Long)

    // Error handling

    /**
     * Get the last error message from the native library.
     */
    external fun getErrorMessage(): String?

    // I/O operations

    /**
     * Parse GBLN file into a value pointer.
     * Returns 0 if parsing fails. Call getErrorMessage() for details.
     */
    external fun parseFile(path: String): Long

    /**
     * Write value to GBLN file.
     * Returns 0 on success, non-zero on error.
     */
    external fun writeFile(path: String, valuePtr: Long): Int
}

/**
 * RAII wrapper for native value pointers.
 *
 * Automatically frees the native pointer when the object is garbage collected.
 * This prevents memory leaks from forgotten valueFree() calls.
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
            FfiWrapper.valueFree(ptr)
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

/**
 * Extension function to check for null pointer and throw appropriate error.
 */
internal fun Long.checkNotNull(operation: String): Long {
    if (this == 0L) {
        val errorMsg = FfiWrapper.getErrorMessage() ?: "Unknown error"
        throw GblnError.NullPointer("$operation failed: $errorMsg")
    }
    return this
}
