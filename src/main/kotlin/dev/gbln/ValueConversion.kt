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
 * Bidirectional conversion between Kotlin values and GBLN native pointers.
 *
 * Provides automatic type selection for integers and strings, choosing the
 * smallest GBLN type that can hold the value.
 */
internal object ValueConversion {

    /**
     * Convert Kotlin value to GBLN native pointer.
     * Returns a ManagedValue that automatically frees the pointer.
     */
    fun toGbln(value: Any?): ManagedValue {
        val ptr = when (value) {
            null -> FfiWrapper.valueNewNull()

            is Boolean -> FfiWrapper.valueNewBool(value)

            // Integer types - auto-select smallest type
            is Byte -> FfiWrapper.valueNewI8(value)
            is Short -> FfiWrapper.valueNewI16(value)
            is Int -> autoSelectIntType(value.toLong())
            is Long -> autoSelectIntType(value)

            // Unsigned integers (represented as next larger signed type in Kotlin)
            is UByte -> FfiWrapper.valueNewU8(value.toShort())
            is UShort -> FfiWrapper.valueNewU16(value.toInt())
            is UInt -> FfiWrapper.valueNewU32(value.toLong())
            is ULong -> FfiWrapper.valueNewU64(value.toLong())

            // Floating-point
            is Float -> FfiWrapper.valueNewF32(value)
            is Double -> FfiWrapper.valueNewF64(value)

            // String - auto-select max length
            is String -> autoSelectStringType(value)

            // Map to GBLN object
            is Map<*, *> -> {
                val objPtr = FfiWrapper.valueNewObject()
                value.forEach { (k, v) ->
                    val key = k.toString()
                    val valuePtr = toGbln(v).pointer()
                    FfiWrapper.objectSet(objPtr, key, valuePtr)
                }
                objPtr
            }

            // List/Array to GBLN array
            is List<*> -> {
                val arrayPtr = FfiWrapper.valueNewArray()
                value.forEach { element ->
                    val elementPtr = toGbln(element).pointer()
                    FfiWrapper.arrayPush(arrayPtr, elementPtr)
                }
                arrayPtr
            }

            is Array<*> -> {
                val arrayPtr = FfiWrapper.valueNewArray()
                value.forEach { element ->
                    val elementPtr = toGbln(element).pointer()
                    FfiWrapper.arrayPush(arrayPtr, elementPtr)
                }
                arrayPtr
            }

            else -> throw GblnError.ConversionError(
                value::class.simpleName ?: "Unknown",
                "GBLN"
            )
        }

        return ManagedValue(ptr.checkNotNull("Value conversion"))
    }

    /**
     * Convert GBLN native pointer to Kotlin value.
     * Returns null if the pointer is invalid.
     */
    fun fromGbln(ptr: Long): Any? {
        if (ptr == 0L) return null

        return when {
            FfiWrapper.valueIsNull(ptr) -> null
            FfiWrapper.valueIsBool(ptr) -> FfiWrapper.valueAsBool(ptr)

            // Signed integers
            FfiWrapper.valueIsI8(ptr) -> FfiWrapper.valueAsI8(ptr)
            FfiWrapper.valueIsI16(ptr) -> FfiWrapper.valueAsI16(ptr)
            FfiWrapper.valueIsI32(ptr) -> FfiWrapper.valueAsI32(ptr)
            FfiWrapper.valueIsI64(ptr) -> FfiWrapper.valueAsI64(ptr)

            // Unsigned integers (return as next larger signed type)
            FfiWrapper.valueIsU8(ptr) -> FfiWrapper.valueAsU8(ptr)
            FfiWrapper.valueIsU16(ptr) -> FfiWrapper.valueAsU16(ptr)
            FfiWrapper.valueIsU32(ptr) -> FfiWrapper.valueAsU32(ptr)
            FfiWrapper.valueIsU64(ptr) -> FfiWrapper.valueAsU64(ptr)

            // Floating-point
            FfiWrapper.valueIsF32(ptr) -> FfiWrapper.valueAsF32(ptr)
            FfiWrapper.valueIsF64(ptr) -> FfiWrapper.valueAsF64(ptr)

            // String
            FfiWrapper.valueIsString(ptr) -> FfiWrapper.valueAsString(ptr)

            // Object - convert to Map
            FfiWrapper.valueIsObject(ptr) -> {
                val keys = FfiWrapper.objectKeys(ptr)
                val map = mutableMapOf<String, Any?>()
                keys.forEach { key ->
                    val valuePtr = FfiWrapper.objectGet(ptr, key)
                    map[key] = fromGbln(valuePtr)
                }
                map
            }

            // Array - convert to List
            FfiWrapper.valueIsArray(ptr) -> {
                val len = FfiWrapper.arrayLen(ptr)
                val list = mutableListOf<Any?>()
                for (i in 0 until len) {
                    val elementPtr = FfiWrapper.arrayGet(ptr, i)
                    list.add(fromGbln(elementPtr))
                }
                list
            }

            else -> throw GblnError.ConversionError("GBLN", "Kotlin")
        }
    }

    /**
     * Automatically select the smallest integer type that can hold the value.
     */
    private fun autoSelectIntType(value: Long): Long {
        return when {
            value in Byte.MIN_VALUE..Byte.MAX_VALUE ->
                FfiWrapper.valueNewI8(value.toByte())

            value in Short.MIN_VALUE..Short.MAX_VALUE ->
                FfiWrapper.valueNewI16(value.toShort())

            value in Int.MIN_VALUE..Int.MAX_VALUE ->
                FfiWrapper.valueNewI32(value.toInt())

            else -> FfiWrapper.valueNewI64(value)
        }
    }

    /**
     * Automatically select the appropriate string type based on length.
     * Uses powers of 2: s2, s4, s8, s16, s32, s64, s128, s256, s512, s1024
     */
    private fun autoSelectStringType(value: String): Long {
        val charCount = value.codePointCount(0, value.length)

        val maxLen = when {
            charCount <= 2 -> 2
            charCount <= 4 -> 4
            charCount <= 8 -> 8
            charCount <= 16 -> 16
            charCount <= 32 -> 32
            charCount <= 64 -> 64
            charCount <= 128 -> 128
            charCount <= 256 -> 256
            charCount <= 512 -> 512
            charCount <= 1024 -> 1024
            else -> throw GblnError.StringTooLong(charCount, 1024, value)
        }

        return FfiWrapper.valueNewString(value, maxLen)
    }
}

/**
 * Extension function to convert any Kotlin value to GBLN.
 */
fun Any?.toGbln(): ManagedValue = ValueConversion.toGbln(this)

/**
 * Extension function to convert GBLN pointer to Kotlin value.
 */
fun Long.fromGbln(): Any? = ValueConversion.fromGbln(this)
