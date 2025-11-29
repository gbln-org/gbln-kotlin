/*
 * Debug test to understand the structure
 */

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "gbln.h"

int main() {
    // Parse simple value and see what we get
    struct GblnValue *value = NULL;
    enum GblnErrorCode result = gbln_parse("age<i8>(25)", &value);
    
    printf("Parse result: %d\n", result);
    if (value != NULL) {
        enum GblnValueType type = gbln_value_type(value);
        printf("Value type: %d\n", type);
        
        char *str = gbln_to_string(value);
        printf("Serialised: %s\n", str);
        if (str) free(str);
        
        // If it's an object, try to get the value
        if (type == Object) {
            printf("It's an object! Checking object_len...\n");
            uintptr_t len = gbln_object_len(value);
            printf("Object length: %lu\n", len);
            
            // Try to get "age" key
            const struct GblnValue *age_val = gbln_object_get(value, "age");
            if (age_val != NULL) {
                printf("Found 'age' key!\n");
                enum GblnValueType age_type = gbln_value_type(age_val);
                printf("Age type: %d\n", age_type);
                
                bool ok = false;
                int8_t val = gbln_value_as_i8(age_val, &ok);
                printf("Age value: %d, ok: %d\n", val, ok);
            } else {
                printf("'age' key not found\n");
            }
        }
        
        gbln_value_free(value);
    }
    
    return 0;
}
