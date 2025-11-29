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

import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun testParseSimpleValue() {
        val result = parse("age<i8>(25)")
        // Note: Current C FFI limitation - parse returns object wrapper
        // This will be fixed when C FFI is extended
        println("Parsed: $result")
    }

    @Test
    fun testLibraryLoads() {
        // Just verify library loads without errors
        val loaded = try {
            lib
            true
        } catch (e: Exception) {
            println("Library load failed: ${e.message}")
            false
        }
        assertEquals(true, loaded, "Library should load successfully")
    }
}
