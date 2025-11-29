/*
 * Correct functional test understanding the API
 * Compile: gcc -I../../core/ffi/include correct_test.c -L../../core/ffi/libs/macos-arm64 -lgbln -o correct_test
 * Run: DYLD_LIBRARY_PATH=../../core/ffi/libs/macos-arm64 ./correct_test
 */

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "gbln.h"

int passed = 0;
int failed = 0;

void test(const char *name, bool (*func)()) {
    printf("Test: %s ... ", name);
    if (func()) {
        printf("✅ PASS\n");
        passed++;
    } else {
        printf("❌ FAIL\n");
        failed++;
    }
}

bool test1() {
    struct GblnValue *value = NULL;
    if (gbln_parse("age<i8>(25)", &value) != Ok) return false;
    
    const struct GblnValue *age_val = gbln_object_get(value, "age");
    if (!age_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    int8_t val = gbln_value_as_i8(age_val, &ok);
    gbln_value_free(value);
    return ok && val == 25;
}

bool test2() {
    struct GblnValue *value = NULL;
    if (gbln_parse("name<s32>(Alice)", &value) != Ok) return false;
    
    const struct GblnValue *name_val = gbln_object_get(value, "name");
    if (!name_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    char *str = gbln_value_as_string(name_val, &ok);
    bool result = ok && strcmp(str, "Alice") == 0;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test3() {
    struct GblnValue *value = NULL;
    if (gbln_parse("user{id<u32>(12345) name<s64>(Alice)}", &value) != Ok) return false;
    
    const struct GblnValue *user_val = gbln_object_get(value, "user");
    if (!user_val) { gbln_value_free(value); return false; }
    
    const struct GblnValue *name_val = gbln_object_get(user_val, "name");
    if (!name_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    char *str = gbln_value_as_string(name_val, &ok);
    bool result = ok && strcmp(str, "Alice") == 0;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test4() {
    struct GblnValue *value = NULL;
    if (gbln_parse("tags<s16>[kotlin jvm android]", &value) != Ok) return false;
    
    const struct GblnValue *tags_val = gbln_object_get(value, "tags");
    if (!tags_val) { gbln_value_free(value); return false; }
    
    if (gbln_value_type(tags_val) != Array) { gbln_value_free(value); return false; }
    if (gbln_array_len(tags_val) != 3) { gbln_value_free(value); return false; }
    
    const struct GblnValue *first = gbln_array_get(tags_val, 0);
    if (!first) { gbln_value_free(value); return false; }
    
    bool ok = false;
    char *str = gbln_value_as_string(first, &ok);
    bool result = ok && strcmp(str, "kotlin") == 0;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test5() {
    struct GblnValue *value = NULL;
    if (gbln_parse("city<s16>(北京)", &value) != Ok) return false;
    
    const struct GblnValue *city_val = gbln_object_get(value, "city");
    if (!city_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    char *str = gbln_value_as_string(city_val, &ok);
    bool result = ok && strcmp(str, "北京") == 0;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test6() {
    struct GblnValue *value = NULL;
    if (gbln_parse("response{status<u16>(200) data{user{name<s32>(Alice)}}}", &value) != Ok) return false;
    
    const struct GblnValue *response = gbln_object_get(value, "response");
    if (!response) { gbln_value_free(value); return false; }
    
    const struct GblnValue *data = gbln_object_get(response, "data");
    if (!data) { gbln_value_free(value); return false; }
    
    const struct GblnValue *user = gbln_object_get(data, "user");
    if (!user) { gbln_value_free(value); return false; }
    
    const struct GblnValue *name = gbln_object_get(user, "name");
    if (!name) { gbln_value_free(value); return false; }
    
    bool ok = false;
    char *str = gbln_value_as_string(name, &ok);
    bool result = ok && strcmp(str, "Alice") == 0;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test7() {
    struct GblnValue *value = NULL;
    if (gbln_parse("name<s32>(Bob)", &value) != Ok) return false;
    
    char *str = gbln_to_string(value);
    bool result = str && strstr(str, "Bob") != NULL;
    if (str) free(str);
    gbln_value_free(value);
    return result;
}

bool test8() {
    struct GblnValue *value = NULL;
    enum GblnErrorCode result = gbln_parse("age<i8>(999)", &value);
    if (value) gbln_value_free(value);
    return result != Ok;
}

bool test9() {
    struct GblnValue *value = NULL;
    if (gbln_parse("active<b>(t)", &value) != Ok) return false;
    
    const struct GblnValue *active_val = gbln_object_get(value, "active");
    if (!active_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    bool val = gbln_value_as_bool(active_val, &ok);
    gbln_value_free(value);
    return ok && val == true;
}

bool test10() {
    struct GblnValue *value = NULL;
    if (gbln_parse("price<f32>(19.99)", &value) != Ok) return false;
    
    const struct GblnValue *price_val = gbln_object_get(value, "price");
    if (!price_val) { gbln_value_free(value); return false; }
    
    bool ok = false;
    float val = gbln_value_as_f32(price_val, &ok);
    gbln_value_free(value);
    return ok && val > 19.98 && val < 20.0;
}

int main() {
    printf("GBLN Kotlin Bindings - Functional Tests\n");
    printf("========================================\n\n");
    
    test("Parse simple integer", test1);
    test("Parse simple string", test2);
    test("Parse object", test3);
    test("Parse array", test4);
    test("Parse UTF-8 string", test5);
    test("Parse nested object", test6);
    test("Serialise to string", test7);
    test("Error handling - integer out of range", test8);
    test("Parse boolean", test9);
    test("Parse float", test10);
    
    printf("\n");
    printf("========================================\n");
    printf("Results: %d passed, %d failed\n", passed, failed);
    printf("========================================\n");
    
    return failed > 0 ? 1 : 0;
}
