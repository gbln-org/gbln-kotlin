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

/**
 * GBLN serialiser API.
 *
 * Provides functions to serialise Kotlin values to GBLN strings.
 * Pattern follows bindings/python/src/gbln/serialise.py exactly.
 */

/**
 * Serialise value to GBLN string.
 *
 * @param value ManagedGblnValue (from parseRaw)
 * @param mini If true, use compact format (no whitespace). Default: true
 * @return GBLN-formatted string
 * @throws SerialiseError if serialisation fails
 */
fun toString(value: ManagedGblnValue, mini: Boolean = true): String {
    // Call appropriate C function
    val cStrPtr = if (mini) {
        lib.gbln_to_string(value.ptr)
    } else {
        lib.gbln_to_string_pretty(value.ptr)
    }

    if (cStrPtr == null || Pointer.nativeValue(cStrPtr) == 0L) {
        throw SerialiseError("Serialisation failed (null pointer returned)")
    }

    // Convert to Kotlin string
    return try {
        cStrPtr.getString(0, "UTF-8")
    } finally {
        // Free C string
        com.sun.jna.Native.free(Pointer.nativeValue(cStrPtr))
    }
}

/**
 * Serialise value to pretty-printed GBLN string.
 *
 * @param value ManagedGblnValue (from parseRaw)
 * @return Pretty-printed GBLN string
 * @throws SerialiseError if serialisation fails
 */
fun toStringPretty(value: ManagedGblnValue): String = toString(value, mini = false)
