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
            null -> FfiWrapper.gblnValueNewNull()

            is Boolean -> FfiWrapper.gblnValueNewBool(value)

            // Integer types - auto-select smallest type
            is Byte -> FfiWrapper.gblnValueNewI8(value)
            is Short -> FfiWrapper.gblnValueNewI16(value)
            is Int -> autoSelectIntType(value.toLong())
            is Long -> autoSelectIntType(value)

            // Unsigned integers (represented as next larger signed type in Kotlin)
            is UByte -> FfiWrapper.gblnValueNewU8(value.toShort())
            is UShort -> FfiWrapper.gblnValueNewU16(value.toInt())
            is UInt -> FfiWrapper.gblnValueNewU32(value.toLong())
            is ULong -> FfiWrapper.gblnValueNewU64(value.toLong())

            // Floating-point
            is Float -> FfiWrapper.gblnValueNewF32(value)
            is Double -> FfiWrapper.gblnValueNewF64(value)

            // String - auto-select max length
            is String -> autoSelectStringType(value)

            // Map to GBLN object
            is Map<*, *> -> {
                val objPtr = FfiWrapper.gblnValueNewObject()
                value.forEach { (k, v) ->
                    val key = k.toString()
                    val valuePtr = toGbln(v).pointer()
                    val errorCode = FfiWrapper.gblnObjectSet(objPtr, key, valuePtr)
                    if (errorCode != FfiWrapper.ErrorCode.OK) {
                        val errorMsg = FfiWrapper.gblnErrorMessage(errorCode)
                        throw GblnError.SerialiseError("Failed to set object key '$key': $errorMsg")
                    }
                }
                objPtr
            }

            // List/Array to GBLN array
            is List<*> -> {
                val arrayPtr = FfiWrapper.gblnValueNewArray()
                value.forEach { element ->
                    val elementPtr = toGbln(element).pointer()
                    val errorCode = FfiWrapper.gblnArrayPush(arrayPtr, elementPtr)
                    if (errorCode != FfiWrapper.ErrorCode.OK) {
                        val errorMsg = FfiWrapper.gblnErrorMessage(errorCode)
                        throw GblnError.SerialiseError("Failed to push array element: $errorMsg")
                    }
                }
                arrayPtr
            }

            is Array<*> -> {
                val arrayPtr = FfiWrapper.gblnValueNewArray()
                value.forEach { element ->
                    val elementPtr = toGbln(element).pointer()
                    val errorCode = FfiWrapper.gblnArrayPush(arrayPtr, elementPtr)
                    if (errorCode != FfiWrapper.ErrorCode.OK) {
                        val errorMsg = FfiWrapper.gblnErrorMessage(errorCode)
                        throw GblnError.SerialiseError("Failed to push array element: $errorMsg")
                    }
                }
                arrayPtr
            }

            else -> throw GblnError.ConversionError(
                value::class.simpleName ?: "Unknown",
                "GBLN"
            )
        }

        if (ptr == 0L) {
            throw GblnError.NullPointer("Failed to create GBLN value")
        }

        return ManagedValue(ptr)
    }

    /**
     * Convert GBLN native pointer to Kotlin value.
     * Returns null if the pointer is invalid.
     */
    fun fromGbln(ptr: Long): Any? {
        if (ptr == 0L) return null

        val valueType = FfiWrapper.gblnValueType(ptr)

        return when (valueType) {
            FfiWrapper.ValueType.NULL -> null

            FfiWrapper.ValueType.BOOL -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsBool(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get boolean value")
                }
                result
            }

            // Signed integers
            FfiWrapper.ValueType.I8 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsI8(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get i8 value")
                }
                result
            }

            FfiWrapper.ValueType.I16 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsI16(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get i16 value")
                }
                result
            }

            FfiWrapper.ValueType.I32 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsI32(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get i32 value")
                }
                result
            }

            FfiWrapper.ValueType.I64 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsI64(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get i64 value")
                }
                result
            }

            // Unsigned integers (return as next larger signed type)
            FfiWrapper.ValueType.U8 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsU8(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get u8 value")
                }
                result
            }

            FfiWrapper.ValueType.U16 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsU16(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get u16 value")
                }
                result
            }

            FfiWrapper.ValueType.U32 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsU32(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get u32 value")
                }
                result
            }

            FfiWrapper.ValueType.U64 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsU64(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get u64 value")
                }
                result
            }

            // Floating-point
            FfiWrapper.ValueType.F32 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsF32(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get f32 value")
                }
                result
            }

            FfiWrapper.ValueType.F64 -> {
                val ok = BooleanArray(1)
                val result = FfiWrapper.gblnValueAsF64(ptr, ok)
                if (!ok[0]) {
                    throw GblnError.TypeError("Failed to get f64 value")
                }
                result
            }

            // String
            FfiWrapper.ValueType.STRING -> {
                FfiHelpers.getString(ptr)
            }

            // Object - convert to Map
            FfiWrapper.ValueType.OBJECT -> {
                val keys = FfiWrapper.gblnObjectKeys(ptr)
                val map = mutableMapOf<String, Any?>()
                keys.forEach { key ->
                    val valuePtr = FfiWrapper.gblnObjectGet(ptr, key)
                    if (valuePtr != 0L) {
                        map[key] = fromGbln(valuePtr)
                    }
                }
                map
            }

            // Array - convert to List
            FfiWrapper.ValueType.ARRAY -> {
                val len = FfiWrapper.gblnArrayLen(ptr)
                val list = mutableListOf<Any?>()
                for (i in 0 until len) {
                    val elementPtr = FfiWrapper.gblnArrayGet(ptr, i)
                    if (elementPtr != 0L) {
                        list.add(fromGbln(elementPtr))
                    }
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
                FfiWrapper.gblnValueNewI8(value.toByte())

            value in Short.MIN_VALUE..Short.MAX_VALUE ->
                FfiWrapper.gblnValueNewI16(value.toShort())

            value in Int.MIN_VALUE..Int.MAX_VALUE ->
                FfiWrapper.gblnValueNewI32(value.toInt())

            else -> FfiWrapper.gblnValueNewI64(value)
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

        return FfiWrapper.gblnValueNewString(value, maxLen)
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
