/*
 * Functional test for Kotlin bindings against libgbln.dylib
 * Run with: kotlinc -cp . functional_test.kt -include-runtime -d test.jar && java -Djava.library.path=../../core/ffi/libs/macos-arm64 -jar test.jar
 */

import java.io.File

// Load native library
System.load(File("../../core/ffi/libs/macos-arm64/libgbln.dylib").absolutePath)

// Simple JNI wrapper for testing
object GblnNative {
    external fun gbln_parse(input: String, outValue: LongArray): Int
    external fun gbln_to_string(valuePtr: Long): String?
    external fun gbln_value_free(valuePtr: Long)
    external fun gbln_get_error_message(): String?

    external fun gbln_value_is_i8(valuePtr: Long): Boolean
    external fun gbln_value_is_string(valuePtr: Long): Boolean
    external fun gbln_value_is_object(valuePtr: Long): Boolean
    external fun gbln_value_is_array(valuePtr: Long): Boolean

    external fun gbln_value_as_i8(valuePtr: Long): Byte
    external fun gbln_value_as_string(valuePtr: Long): String?

    external fun gbln_object_get(objectPtr: Long, key: String): Long
    external fun gbln_object_len(objectPtr: Long): Int

    external fun gbln_array_len(arrayPtr: Long): Int
    external fun gbln_array_get(arrayPtr: Long, index: Int): Long
}

fun main() {
    var passed = 0
    var failed = 0

    fun test(name: String, block: () -> Boolean) {
        print("Test: $name ... ")
        try {
            if (block()) {
                println("✅ PASS")
                passed++
            } else {
                println("❌ FAIL")
                failed++
            }
        } catch (e: Exception) {
            println("❌ EXCEPTION: ${e.message}")
            failed++
        }
    }

    test("Parse simple integer") {
        val outValue = LongArray(1)
        val result = GblnNative.gbln_parse("age<i8>(25)", outValue)
        if (result != 0) {
            println("Parse failed: ${GblnNative.gbln_get_error_message()}")
            return@test false
        }
        val ptr = outValue[0]
        val isI8 = GblnNative.gbln_value_is_i8(ptr)
        val value = GblnNative.gbln_value_as_i8(ptr)
        GblnNative.gbln_value_free(ptr)
        isI8 && value == 25.toByte()
    }

    test("Parse simple string") {
        val outValue = LongArray(1)
        val result = GblnNative.gbln_parse("name<s32>(Alice)", outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]
        val isString = GblnNative.gbln_value_is_string(ptr)
        val value = GblnNative.gbln_value_as_string(ptr)
        GblnNative.gbln_value_free(ptr)
        isString && value == "Alice"
    }

    test("Parse object") {
        val outValue = LongArray(1)
        val input = "user{id<u32>(12345) name<s64>(Alice)}"
        val result = GblnNative.gbln_parse(input, outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]
        val isObject = GblnNative.gbln_value_is_object(ptr)
        val len = GblnNative.gbln_object_len(ptr)

        val namePtr = GblnNative.gbln_object_get(ptr, "name")
        val name = GblnNative.gbln_value_as_string(namePtr)

        GblnNative.gbln_value_free(ptr)
        isObject && len == 2 && name == "Alice"
    }

    test("Parse array") {
        val outValue = LongArray(1)
        val input = "tags<s16>[kotlin jvm android]"
        val result = GblnNative.gbln_parse(input, outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]
        val isArray = GblnNative.gbln_value_is_array(ptr)
        val len = GblnNative.gbln_array_len(ptr)

        val firstPtr = GblnNative.gbln_array_get(ptr, 0)
        val first = GblnNative.gbln_value_as_string(firstPtr)

        GblnNative.gbln_value_free(ptr)
        isArray && len == 3 && first == "kotlin"
    }

    test("Parse UTF-8 string") {
        val outValue = LongArray(1)
        val input = "city<s16>(北京)"
        val result = GblnNative.gbln_parse(input, outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]
        val value = GblnNative.gbln_value_as_string(ptr)
        GblnNative.gbln_value_free(ptr)
        value == "北京"
    }

    test("Parse nested object") {
        val outValue = LongArray(1)
        val input = "response{status<u16>(200) data{user{name<s32>(Alice)}}}"
        val result = GblnNative.gbln_parse(input, outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]

        val dataPtr = GblnNative.gbln_object_get(ptr, "data")
        val userPtr = GblnNative.gbln_object_get(dataPtr, "user")
        val namePtr = GblnNative.gbln_object_get(userPtr, "name")
        val name = GblnNative.gbln_value_as_string(namePtr)

        GblnNative.gbln_value_free(ptr)
        name == "Alice"
    }

    test("Serialise to string") {
        val outValue = LongArray(1)
        val input = "name<s32>(Bob)"
        val result = GblnNative.gbln_parse(input, outValue)
        if (result != 0) return@test false
        val ptr = outValue[0]

        val serialised = GblnNative.gbln_to_string(ptr)
        GblnNative.gbln_value_free(ptr)

        serialised?.contains("Bob") == true
    }

    test("Error handling - integer out of range") {
        val outValue = LongArray(1)
        val input = "age<i8>(999)"
        val result = GblnNative.gbln_parse(input, outValue)
        result != 0  // Should fail
    }

    println("\n" + "=".repeat(50))
    println("Results: $passed passed, $failed failed")
    println("=".repeat(50))

    if (failed > 0) {
        System.exit(1)
    }
}
