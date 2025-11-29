# Kotlin Bindings - Status Report

**Date**: 2025-11-29  
**Ticket**: #103  
**Status**: ⚠️ INCOMPLETE - Requires Complete Rewrite

---

## Summary

Initial Kotlin bindings implementation completed but **INCORRECT**. All code must be rewritten to match actual C FFI API.

## What Was Done

- ✅ 25 files created (3,941 lines of code)
- ✅ Project structure correct (Gradle, tests, docs)
- ✅ `.gitignore` created FIRST (lesson from #101)
- ✅ All files have Apache 2.0 headers
- ✅ Comprehensive test suite written (100+ tests)
- ✅ Complete README.md documentation

## Critical Problem

**The entire implementation is based on WRONG API assumptions!**

### What I Assumed (WRONG)

```kotlin
// WRONG - these functions don't exist in C FFI
external fun gbln_parse(input: String): Long
external fun gbln_value_is_i8(ptr: Long): Boolean
external fun gbln_value_is_string(ptr: Long): Boolean
// etc.
```

### Actual C FFI API

```c
// Correct API from core/ffi/include/gbln.h
enum GblnErrorCode gbln_parse(const char *input, struct GblnValue **out_value);
enum GblnValueType gbln_value_type(const struct GblnValue *value);
int8_t gbln_value_as_i8(const struct GblnValue *value, bool *ok);
// etc.
```

### Key Differences

1. **Parse returns Object wrapper**: `gbln_parse("age<i8>(25)")` returns `{age<i8>(25)}` not just `25`
2. **No separate is_* functions**: Use `gbln_value_type() == I8` not `gbln_value_is_i8()`
3. **Values need OK flag**: `gbln_value_as_i8(value, &ok)` not just `gbln_value_as_i8(value)`
4. **Error handling different**: Returns `enum GblnErrorCode` not throws

## Verified Working

**C FFI Library**: ✅ 10/10 functional tests passing

```bash
$ DYLD_LIBRARY_PATH=../../core/ffi/libs/macos-arm64 ./correct_test
Test: Parse simple integer ... ✅ PASS
Test: Parse simple string ... ✅ PASS
Test: Parse object ... ✅ PASS
Test: Parse array ... ✅ PASS
Test: Parse UTF-8 string ... ✅ PASS
Test: Parse nested object ... ✅ PASS
Test: Serialise to string ... ✅ PASS
Test: Error handling ... ✅ PASS
Test: Parse boolean ... ✅ PASS
Test: Parse float ... ✅ PASS

Results: 10 passed, 0 failed
```

## What Needs Rewrite

**Everything that touches FFI:**

1. ❌ `src/main/cpp/gbln_jni.cpp` (599 lines) - Complete rewrite
2. ❌ `src/main/cpp/gbln_jni.h` (213 lines) - Complete rewrite
3. ❌ `src/main/kotlin/dev/gbln/FfiWrapper.kt` (170 lines) - Complete rewrite
4. ❌ `src/main/kotlin/dev/gbln/ValueConversion.kt` (153 lines) - Complete rewrite
5. ❌ `src/main/kotlin/dev/gbln/Parser.kt` (132 lines) - Rewrite
6. ❌ `src/main/kotlin/dev/gbln/Serialiser.kt` (155 lines) - Rewrite
7. ❌ `src/main/kotlin/dev/gbln/Io.kt` (207 lines) - Rewrite
8. ❌ All test files - Adjust expectations

**Can keep:**

- ✅ `Error.kt` - Sealed class structure is fine (just adjust error types)
- ✅ `Config.kt` - Configuration structure OK
- ✅ `Gbln.kt` - Public API structure OK (just delegate to corrected internals)
- ✅ Build files, README, LICENSE - All fine

## Reference Implementation

**MUST follow**: `bindings/python/` (Ticket #100)

Key files to study:
- `bindings/python/src/gbln/ffi.py` - Correct FFI layer
- `bindings/python/src/gbln/value.py` - Correct value conversion
- `bindings/python/src/gbln/parse.py` - Correct parser
- `docs/builds/BINDING_BUILDS.md` - Complete guide

## Lessons Learned

1. ❌ **NEVER assume API** - Always read actual header files first
2. ❌ **ALWAYS check reference implementation** - Python (#100) is the reference
3. ❌ **Verify with simple test first** - Should have tested C API before writing 4000 lines
4. ✅ **Documentation exists** - `BINDING_BUILDS.md` has everything, should have read it first

## Next Steps (New Session)

1. Read `core/ffi/include/gbln.h` completely
2. Study Python FFI implementation line by line
3. Write simple JNI test against actual API
4. Only then start implementing Kotlin wrapper
5. Test incrementally, not all at once

## Files to Review

- `correct_test.c` - Working C test showing correct API usage
- `debug_test.c` - Shows how parse returns Object wrapper
- `bindings/python/src/gbln/ffi.py` - Reference FFI implementation

---

**Estimated Time for Correct Implementation**: 2-3 hours

**Current Code Status**: Structurally complete but functionally incorrect - requires full rewrite of FFI layer
