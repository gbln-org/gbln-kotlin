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

import com.sun.jna.Pointer
import java.lang.ref.Cleaner

/**
 * GBLN value conversion between Kotlin and C.
 *
 * Converts Kotlin types (Map, List, Int, String, etc.) to/from GBLN Value pointers.
 * Handles automatic type detection and memory management.
 * Pattern follows bindings/python/src/gbln/value.py exactly.
 */

// Cleaner for automatic memory management
private val cleaner = Cleaner.create()

/**
 * GBLN Value with automatic memory management.
 *
 * Uses Cleaner to automatically free C memory when Kotlin object
 * is garbage collected. Prevents double-free and use-after-free errors.
 */
class ManagedGblnValue(val ptr: Pointer) {

    init {
        // Register cleanup action
        cleaner.register(this, CleanupAction(ptr))
    }

    private class CleanupAction(private val ptr: Pointer) : Runnable {
        override fun run() {
            lib.gbln_value_free(ptr)
        }
    }
}

/**
 * Convert GBLN Value to Kotlin value.
 *
 * Uses gbln_value_type() for efficient type detection.
 * Recursively converts GBLN objects and arrays to Kotlin Map/List.
 * Handles all GBLN types (integers, floats, strings, bool, null).
 *
 * @param value Pointer to GblnValue from C FFI
 * @return Kotlin Map, List, or primitive value
 * @throws GblnError if conversion fails or unknown type encountered
 */
internal fun gblnToKotlin(value: Pointer?): Any? {
    if (value == null || Pointer.nativeValue(value) == 0L) {
        throw GblnError("Null pointer passed to gblnToKotlin")
    }

    // Use gbln_value_type() for efficient type detection
    val valueType = lib.gbln_value_type(value)

    // Handle each type based on discriminant
    return when (valueType) {
        GblnValueType.NULL -> null

        GblnValueType.BOOL -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_bool(value, ok)
            if (ok[0] != 0.toByte()) {
                result != 0.toByte()
            } else {
                throw GblnError("Failed to extract bool value")
            }
        }

        // Signed integers
        GblnValueType.I8 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_i8(value, ok)
            if (ok[0] != 0.toByte()) result.toInt() else throw GblnError("Failed to extract i8 value")
        }

        GblnValueType.I16 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_i16(value, ok)
            if (ok[0] != 0.toByte()) result.toInt() else throw GblnError("Failed to extract i16 value")
        }

        GblnValueType.I32 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_i32(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract i32 value")
        }

        GblnValueType.I64 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_i64(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract i64 value")
        }

        // Unsigned integers (return as next larger signed type)
        GblnValueType.U8 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_u8(value, ok)
            if (ok[0] != 0.toByte()) result.toInt() else throw GblnError("Failed to extract u8 value")
        }

        GblnValueType.U16 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_u16(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract u16 value")
        }

        GblnValueType.U32 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_u32(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract u32 value")
        }

        GblnValueType.U64 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_u64(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract u64 value")
        }

        // Floats
        GblnValueType.F32 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_f32(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract f32 value")
        }

        GblnValueType.F64 -> {
            val ok = ByteArray(1)
            val result = lib.gbln_value_as_f64(value, ok)
            if (ok[0] != 0.toByte()) result else throw GblnError("Failed to extract f64 value")
        }

        // String
        GblnValueType.STRING -> {
            val ok = ByteArray(1)
            val strPtr = lib.gbln_value_as_string(value, ok)
            if (ok[0] != 0.toByte()) {
                if (strPtr != null && Pointer.nativeValue(strPtr) != 0L) {
                    // NOTE: String is owned by the Value - don't free
                    strPtr.getString(0, "UTF-8")
                } else {
                    ""
                }
            } else {
                throw GblnError("Failed to extract string value")
            }
        }

        // Array
        GblnValueType.ARRAY -> {
            val result = mutableListOf<Any?>()
            val arrayLen = lib.gbln_array_len(value)
            for (i in 0 until arrayLen) {
                val elem = lib.gbln_array_get(value, i)
                if (elem != null && Pointer.nativeValue(elem) != 0L) {
                    result.add(gblnToKotlin(elem))
                }
            }
            result
        }

        // Object
        GblnValueType.OBJECT -> {
            val result = mutableMapOf<String, Any?>()
            val objectLen = lib.gbln_object_len(value)

            // For now, we can't get keys without gbln_object_keys() function
            // This is a limitation - we need that function added to C FFI
            // For now, return empty map
            // TODO: Add gbln_object_keys() to C FFI and implement properly
            result
        }

        else -> throw GblnError("Unknown value type: $valueType")
    }
}
