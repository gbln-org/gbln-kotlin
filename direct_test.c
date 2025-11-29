/*
 * Direct test of libgbln.dylib to verify it works before JNI
 * Compile: gcc -I../../core/ffi/include direct_test.c -L../../core/ffi/libs/macos-arm64 -lgbln -o direct_test
 * Run: ./direct_test
 */

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "gbln.h"

int main() {
    printf("Testing libgbln.dylib directly...\n\n");
    
    // Test 1: Parse simple integer
    printf("Test 1: Parse simple integer ... ");
    struct GblnValue *value1 = NULL;
    enum GblnErrorCode result1 = gbln_parse("age<i8>(25)", &value1);
    if (result1 == Ok && value1 != NULL) {
        enum GblnValueType type = gbln_value_type(value1);
        if (type == I8) {
            bool ok = false;
            int8_t val = gbln_value_as_i8(value1, &ok);
            if (ok && val == 25) {
                printf("✅ PASS\n");
            } else {
                printf("❌ FAIL (value: %d, ok: %d)\n", val, ok);
            }
        } else {
            printf("❌ FAIL (wrong type: %d)\n", type);
        }
        gbln_value_free(value1);
    } else {
        printf("❌ FAIL (parse error: %d)\n", result1);
    }
    
    // Test 2: Parse simple string
    printf("Test 2: Parse simple string ... ");
    struct GblnValue *value2 = NULL;
    enum GblnErrorCode result2 = gbln_parse("name<s32>(Alice)", &value2);
    if (result2 == Ok && value2 != NULL) {
        enum GblnValueType type = gbln_value_type(value2);
        if (type == Str) {
            bool ok = false;
            char *str = gbln_value_as_string(value2, &ok);
            if (ok && strcmp(str, "Alice") == 0) {
                printf("✅ PASS\n");
            } else {
                printf("❌ FAIL (value: %s, ok: %d)\n", str, ok);
            }
            if (str) free(str);
        } else {
            printf("❌ FAIL (wrong type: %d)\n", type);
        }
        gbln_value_free(value2);
    } else {
        printf("❌ FAIL (parse error: %d)\n", result2);
    }
    
    // Test 3: Parse object
    printf("Test 3: Parse object ... ");
    struct GblnValue *value3 = NULL;
    enum GblnErrorCode result3 = gbln_parse("user{id<u32>(12345) name<s64>(Alice)}", &value3);
    if (result3 == Ok && value3 != NULL) {
        enum GblnValueType type = gbln_value_type(value3);
        if (type == Object) {
            const struct GblnValue *name_val = gbln_object_get(value3, "name");
            if (name_val != NULL) {
                bool ok = false;
                char *str = gbln_value_as_string(name_val, &ok);
                if (ok && strcmp(str, "Alice") == 0) {
                    printf("✅ PASS\n");
                } else {
                    printf("❌ FAIL (name: %s, ok: %d)\n", str, ok);
                }
                if (str) free(str);
            } else {
                printf("❌ FAIL (name key not found)\n");
            }
        } else {
            printf("❌ FAIL (wrong type: %d)\n", type);
        }
        gbln_value_free(value3);
    } else {
        printf("❌ FAIL (parse error: %d)\n", result3);
    }
    
    // Test 4: Parse array
    printf("Test 4: Parse array ... ");
    struct GblnValue *value4 = NULL;
    enum GblnErrorCode result4 = gbln_parse("tags<s16>[kotlin jvm android]", &value4);
    if (result4 == Ok && value4 != NULL) {
        enum GblnValueType type = gbln_value_type(value4);
        if (type == Array) {
            uintptr_t len = gbln_array_len(value4);
            if (len == 3) {
                const struct GblnValue *first = gbln_array_get(value4, 0);
                if (first != NULL) {
                    bool ok = false;
                    char *str = gbln_value_as_string(first, &ok);
                    if (ok && strcmp(str, "kotlin") == 0) {
                        printf("✅ PASS\n");
                    } else {
                        printf("❌ FAIL (first: %s, ok: %d)\n", str, ok);
                    }
                    if (str) free(str);
                } else {
                    printf("❌ FAIL (first element not found)\n");
                }
            } else {
                printf("❌ FAIL (wrong length: %lu)\n", len);
            }
        } else {
            printf("❌ FAIL (wrong type: %d)\n", type);
        }
        gbln_value_free(value4);
    } else {
        printf("❌ FAIL (parse error: %d)\n", result4);
    }
    
    // Test 5: UTF-8 string
    printf("Test 5: Parse UTF-8 string ... ");
    struct GblnValue *value5 = NULL;
    enum GblnErrorCode result5 = gbln_parse("city<s16>(北京)", &value5);
    if (result5 == Ok && value5 != NULL) {
        bool ok = false;
        char *str = gbln_value_as_string(value5, &ok);
        if (ok && strcmp(str, "北京") == 0) {
            printf("✅ PASS\n");
        } else {
            printf("❌ FAIL\n");
        }
        if (str) free(str);
        gbln_value_free(value5);
    } else {
        printf("❌ FAIL (parse error: %d)\n", result5);
    }
    
    // Test 6: Serialise
    printf("Test 6: Serialise to string ... ");
    struct GblnValue *value6 = NULL;
    enum GblnErrorCode result6 = gbln_parse("name<s32>(Bob)", &value6);
    if (result6 == Ok && value6 != NULL) {
        char *serialised = gbln_to_string(value6);
        if (serialised && strstr(serialised, "Bob") != NULL) {
            printf("✅ PASS\n");
        } else {
            printf("❌ FAIL\n");
        }
        if (serialised) free(serialised);
        gbln_value_free(value6);
    } else {
        printf("❌ FAIL\n");
    }
    
    // Test 7: Error handling
    printf("Test 7: Error handling (integer out of range) ... ");
    struct GblnValue *value7 = NULL;
    enum GblnErrorCode result7 = gbln_parse("age<i8>(999)", &value7);
    if (result7 != Ok) {
        printf("✅ PASS\n");
    } else {
        printf("❌ FAIL (should have failed)\n");
        if (value7) gbln_value_free(value7);
    }
    
    printf("\n✅ All direct C tests completed!\n");
    return 0;
}
