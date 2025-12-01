package dev.gbln

import org.junit.jupiter.api.Test

class DebugTest {
    @Test
    fun `debug what parse returns`() {
        try {
            println("=== Test 1: Simple value ===")
            val age = parse("age<i8>(25)")
            println("age result: $age (${age?.javaClass})")

            println("\n=== Test 2: Simple object ===")
            val user = parse("user{id<u32>(123)}")
            println("user result: $user")
            if (user is Map<*, *>) {
                println("  Keys: ${user.keys}")
                println("  Size: ${user.size}")
                user.forEach { (k, v) -> println("  $k -> $v") }
            }

        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
    }
}
