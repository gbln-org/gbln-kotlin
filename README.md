# GBLN Kotlin

Kotlin bindings for **GBLN (Goblin Bounded Lean Notation)** - the first **type-safe LLM-native serialisation format**.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-JVM%20%7C%20Android-green.svg)](https://kotlinlang.org/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-orange.svg)](https://kotlinlang.org/)

## What is GBLN?

GBLN is a text-based serialisation format that combines the type safety of Protocol Buffers with the readability of JSON, whilst using **86% fewer tokens than JSON in LLM contexts**.

### Key Features

- ✅ **Type-safe**: Parse-time validation with inline type hints
- 🚀 **LLM-optimised**: 86% fewer tokens than JSON for AI contexts
- 💾 **Memory-efficient**: Bounded types prevent waste and vulnerabilities
- 📖 **Human-readable**: Clear syntax, easy to read and edit
- 🔄 **Git-friendly**: Meaningful diffs, ordered keys preserved
- ⚡ **Simple parser**: Single-pass, deterministic, 3 simple rules

### Example

```gbln
:| User profile with type-safe fields
user{
    id<u32>(12345)           :| 32-bit unsigned integer
    name<s64>(Alice Johnson) :| Max 64 characters
    age<i8>(25)              :| 8-bit signed integer
    score<f32>(98.5)         :| 32-bit float
    active<b>(t)             :| Boolean (true/false)
}
```

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.gbln:gbln-kotlin:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'dev.gbln:gbln-kotlin:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>dev.gbln</groupId>
    <artifactId>gbln-kotlin</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```kotlin
import dev.gbln.Gbln

// Parse GBLN string
val data = Gbln.parse("user{id<u32>(12345) name<s64>(Alice)}")
val user = data as Map<String, Any?>
println(user["name"]) // "Alice"

// Serialise Kotlin value to GBLN
val person = mapOf("id" to 123, "name" to "Bob")
val gbln = Gbln.toString(person)
println(gbln) // {id<i8>(123)name<s8>(Bob)}

// Pretty-print for humans
val pretty = Gbln.toPrettyString(person)
println(pretty)
// {
//     id<i8>(123)
//     name<s8>(Bob)
// }

// Read from file
val config = Gbln.readFile("config.gbln")

// Write to file
Gbln.writeFile("output.gbln", data)
```

## Type System

GBLN provides precise type control with parse-time validation:

| Category | Types | Kotlin Mapping | Example |
|----------|-------|----------------|---------|
| **Signed Int** | i8, i16, i32, i64 | Byte, Short, Int, Long | `age<i8>(25)` |
| **Unsigned Int** | u8, u16, u32, u64 | Short, Int, Long, Long | `id<u32>(12345)` |
| **Float** | f32, f64 | Float, Double | `price<f32>(19.99)` |
| **String** | s2-s1024 | String | `name<s64>(Alice)` |
| **Boolean** | b | Boolean | `active<b>(t)` |
| **Null** | n | null | `optional<n>()` |
| **Object** | {...} | Map | `user{...}` |
| **Array** | [...] | List | `tags[...]` |

### String Types

Available string lengths: `s2`, `s4`, `s8`, `s16`, `s32`, `s64`, `s128`, `s256`, `s512`, `s1024`

Length is counted in **UTF-8 characters**, not bytes:
```kotlin
Gbln.parse("city<s4>(北京)") // 2 characters - OK
Gbln.parse("emoji<s8>(Hello🔥)") // 6 characters - OK
```

## Usage Examples

### Parsing

```kotlin
import dev.gbln.Gbln
import dev.gbln.GblnConfig

// Basic parsing
val result = Gbln.parse("name<s32>(Alice)")

// Parse with type assertion
val map = Gbln.parseAs<Map<String, Any?>>(
    "user{id<u32>(123) name<s64>(Alice)}"
)

// Parse complex nested structure
val data = Gbln.parse("""
    response{
        status<u16>(200)
        data{
            users[
                {id<u32>(1) name<s32>(Alice)}
                {id<u32>(2) name<s32>(Bob)}
            ]
        }
    }
""")

// Using extension function
val value = "age<i8>(25)".parseGbln()

// With custom configuration
val config = GblnConfig(
    strictTypes = true,
    prettyPrint = false,
    checkDuplicateKeys = true
)
val parsed = Gbln.parse(input, config)
```

### Serialisation

```kotlin
import dev.gbln.Gbln

// Serialise basic types
Gbln.toString(25) // "<i8>(25)"
Gbln.toString("Alice") // "<s8>(Alice)"
Gbln.toString(true) // "<b>(t)"

// Serialise map
val user = mapOf(
    "id" to 123,
    "name" to "Alice",
    "active" to true
)
Gbln.toString(user)
// {id<i8>(123)name<s8>(Alice)active<b>(t)}

// Serialise list
val tags = listOf("kotlin", "jvm", "android")
Gbln.toString(tags)
// [<s8>(kotlin)<s8>(jvm)<s8>(android)]

// Pretty-print
val pretty = Gbln.toPrettyString(user)
println(pretty)
// {
//     id<i8>(123)
//     name<s8>(Alice)
//     active<b>(t)
// }

// Using extension functions
val gbln = user.toGblnString()
val prettyGbln = user.toGblnPrettyString()
```

### File I/O

```kotlin
import dev.gbln.Gbln
import java.io.File

// Write to file
val data = mapOf(
    "users" to listOf(
        mapOf("id" to 1, "name" to "Alice"),
        mapOf("id" to 2, "name" to "Bob")
    )
)
Gbln.writeFile("users.gbln", data)

// Read from file
val loaded = Gbln.readFile("users.gbln")

// Read with type assertion
val config = Gbln.readFileAs<Map<String, Any?>>("config.gbln")

// Using File extension functions
val file = File("data.gbln")
file.writeGbln(data)
val result = file.readGbln()

// Using Path extension functions
val path = Paths.get("data.gbln")
path.writeGbln(data)
val result = path.readGbln()
```

### Async Operations with Coroutines

```kotlin
import dev.gbln.Gbln
import kotlinx.coroutines.runBlocking

runBlocking {
    // Parse asynchronously
    val data = Gbln.parseAsync(gblnString)
    
    // Serialise asynchronously
    val gbln = Gbln.toStringAsync(data)
    
    // File I/O asynchronously
    Gbln.writeFileAsync("output.gbln", data)
    val loaded = Gbln.readFileAsync("output.gbln")
    
    // Using extension functions
    val value = "name<s32>(Alice)".parseGblnAsync()
    val serialised = user.toGblnStringAsync()
    
    // File extensions
    val file = File("data.gbln")
    file.writeGblnAsync(data)
    val result = file.readGblnAsync()
}
```

### Error Handling

```kotlin
import dev.gbln.*

// Using exceptions
try {
    val data = Gbln.parse(input)
} catch (e: GblnError.ParseError) {
    println("Parse failed: ${e.message}")
} catch (e: GblnError.TypeError) {
    println("Type validation failed: ${e.message}")
} catch (e: GblnError.FileNotFound) {
    println("File not found: ${e.message}")
}

// Using Result type
val result = Gbln.parseResult(input)
result.onSuccess { data ->
    println("Parsed: $data")
}.onFailure { error ->
    println("Error: ${error.message}")
}

// Pattern matching on error types
when (val result = Gbln.parseResult(input)) {
    is GblnResult.Success -> processData(result.value)
    is GblnResult.Failure -> when (result.error) {
        is GblnError.ParseError -> handleParseError(result.error)
        is GblnError.TypeError -> handleTypeError(result.error)
        else -> handleGenericError(result.error)
    }
}

// Railway-Oriented Programming
val processed = Gbln.parseResult(input)
    .map { data -> transform(data) }
    .flatMap { transformed -> validate(transformed) }
    .recover { error -> defaultValue }
```

### Configuration Presets

```kotlin
import dev.gbln.GblnConfig

// Default configuration
val default = GblnConfig.DEFAULT

// LLM-optimised (minimal tokens)
val llm = GblnConfig.LLM_OPTIMISED
val compact = Gbln.toString(data, llm)

// Human-readable
val human = GblnConfig.HUMAN_READABLE
val pretty = Gbln.toString(data, human)

// Debug mode
val debug = GblnConfig.DEBUG
val debugOutput = Gbln.toString(data, debug)

// Custom configuration
val custom = GblnConfig(
    strictTypes = true,
    prettyPrint = true,
    indent = "  ", // 2 spaces
    stripComments = false,
    checkDuplicateKeys = true,
    maxDepth = 128
)
```

## Advanced Features

### Type Auto-Selection

The library automatically selects the smallest type that can hold a value:

```kotlin
// Integer auto-selection
Gbln.toString(25)        // Uses i8 (fits in -128 to 127)
Gbln.toString(1000)      // Uses i16 (fits in -32768 to 32767)
Gbln.toString(100000)    // Uses i32

// String auto-selection
Gbln.toString("Hi")              // Uses s2
Gbln.toString("Hello")           // Uses s8
Gbln.toString("A".repeat(100))   // Uses s128
```

### Nested Structures

```kotlin
val complexData = mapOf(
    "response" to mapOf(
        "status" to 200,
        "message" to "Success",
        "data" to listOf(
            mapOf(
                "id" to 1,
                "name" to "Alice",
                "scores" to listOf(95, 87, 92)
            ),
            mapOf(
                "id" to 2,
                "name" to "Bob",
                "scores" to listOf(88, 91, 85)
            )
        )
    )
)

val gbln = Gbln.toPrettyString(complexData)
val parsed = Gbln.parse(gbln)
```

### Round-Trip Serialisation

```kotlin
// Original → GBLN → Parse → Should match original
val original = mapOf("id" to 123, "name" to "Alice")
val gbln = Gbln.toString(original)
val parsed = Gbln.parse(gbln)

// Values are preserved through round-trip
assertEquals(original["name"], (parsed as Map<*, *>)["name"])
```

## Error Types

The library provides detailed error types for precise error handling:

```kotlin
sealed class GblnError : Exception {
    data class ParseError(val details: String)
    data class TypeError(val details: String)
    data class IntegerOutOfRange(val value: Long, val typeName: String, val min: Long, val max: Long)
    data class StringTooLong(val length: Int, val maxLength: Int, val value: String)
    data class IoError(val details: String)
    data class FileNotFound(val path: String)
    data class PermissionDenied(val path: String)
    data class ConversionError(val fromType: String, val toType: String)
    // ... and more
}
```

## Platform Support

- **JVM**: Java 11+ (tested on Java 11, 17, 21)
- **Android**: API Level 21+ (Android 5.0 Lollipop)
- **Kotlin**: 1.9.22+

## Building from Source

### Prerequisites

- JDK 11 or higher
- Gradle 8.0+
- C++ compiler (for JNI layer)
- GBLN C library (`libgbln.dylib`/`.so`/`.dll`)

### Build Steps

```bash
# Clone repository
git clone https://github.com/gbln-org/gbln-kotlin.git
cd gbln-kotlin

# Build JNI library
./gradlew buildJni

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Publish to local Maven
./gradlew publishToMavenLocal
```

## Performance

GBLN prioritises **type safety** and **token efficiency** over raw parsing speed:

- **Parse speed**: ~30-50% slower than JSON (acceptable trade-off for type safety)
- **Size**: 30-40% smaller than JSON minified
- **LLM tokens**: 86% fewer tokens than JSON in AI contexts
- **Memory**: Bounded types prevent buffer overflows and waste

## Comparison with Other Formats

| Feature | GBLN | JSON | YAML | Protobuf |
|---------|------|------|------|----------|
| Type-safe | ✅ | ❌ | ❌ | ✅ |
| Human-readable | ✅ | ✅ | ✅ | ❌ |
| LLM-optimised | ✅ | ❌ | ❌ | ❌ |
| Bounded types | ✅ | ❌ | ❌ | ✅ |
| Git-friendly | ✅ | ✅ | ✅ | ❌ |
| Simple parser | ✅ | ✅ | ❌ | ❌ |
| Size efficiency | ✅ | ❌ | ❌ | ✅ |

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](../../CONTRIBUTING.md) for guidelines.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

## Links

- **Documentation**: [gbln.dev](https://gbln.dev)
- **Specification**: [GBLN Spec v1.0](../../docs/01-specification.md)
- **GitHub Organisation**: [github.com/gbln-org](https://github.com/gbln-org)
- **Issues**: [github.com/gbln-org/gbln-kotlin/issues](https://github.com/gbln-org/gbln-kotlin/issues)

## Support

- **Email**: ask@vvoss.dev
- **GitHub Issues**: [Report a bug](https://github.com/gbln-org/gbln-kotlin/issues)
- **Discussions**: [GitHub Discussions](https://github.com/gbln-org/gbln/discussions)

---

**GBLN** - Type-safe data that speaks clearly 🦇
