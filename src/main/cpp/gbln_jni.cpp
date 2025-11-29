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

#include "gbln_jni.h"
#include "gbln.h"
#include <cstring>
#include <vector>

// Helper function to convert jstring to C string
static const char* jstringToC(JNIEnv* env, jstring jstr) {
    if (!jstr) return nullptr;
    return env->GetStringUTFChars(jstr, nullptr);
}

// Helper function to release C string
static void releaseJString(JNIEnv* env, jstring jstr, const char* cstr) {
    if (jstr && cstr) {
        env->ReleaseStringUTFChars(jstr, cstr);
    }
}

// Helper function to create Java string
static jstring createJString(JNIEnv* env, const char* cstr) {
    if (!cstr) return nullptr;
    return env->NewStringUTF(cstr);
}

// Parser functions

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_parse(
    JNIEnv* env, jobject obj, jstring input) {
    
    const char* cInput = jstringToC(env, input);
    if (!cInput) return 0;
    
    GblnValue* value = nullptr;
    GblnResult result = gbln_parse(cInput, &value);
    
    releaseJString(env, input, cInput);
    
    if (result != GBLN_OK) {
        return 0;
    }
    
    return reinterpret_cast<jlong>(value);
}

// Serialiser functions

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_toString(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    if (!value) return nullptr;
    
    const char* result = gbln_to_string(value);
    if (!result) return nullptr;
    
    jstring jResult = createJString(env, result);
    gbln_string_free(const_cast<char*>(result));
    
    return jResult;
}

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_toPrettyString(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    if (!value) return nullptr;
    
    const char* result = gbln_to_pretty_string(value);
    if (!result) return nullptr;
    
    jstring jResult = createJString(env, result);
    gbln_string_free(const_cast<char*>(result));
    
    return jResult;
}

// Value creation functions

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI8(
    JNIEnv* env, jobject obj, jbyte value) {
    
    GblnValue* result = gbln_value_new_i8(static_cast<int8_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI16(
    JNIEnv* env, jobject obj, jshort value) {
    
    GblnValue* result = gbln_value_new_i16(static_cast<int16_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI32(
    JNIEnv* env, jobject obj, jint value) {
    
    GblnValue* result = gbln_value_new_i32(static_cast<int32_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI64(
    JNIEnv* env, jobject obj, jlong value) {
    
    GblnValue* result = gbln_value_new_i64(static_cast<int64_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU8(
    JNIEnv* env, jobject obj, jshort value) {
    
    GblnValue* result = gbln_value_new_u8(static_cast<uint8_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU16(
    JNIEnv* env, jobject obj, jint value) {
    
    GblnValue* result = gbln_value_new_u16(static_cast<uint16_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU32(
    JNIEnv* env, jobject obj, jlong value) {
    
    GblnValue* result = gbln_value_new_u32(static_cast<uint32_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU64(
    JNIEnv* env, jobject obj, jlong value) {
    
    GblnValue* result = gbln_value_new_u64(static_cast<uint64_t>(value));
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewF32(
    JNIEnv* env, jobject obj, jfloat value) {
    
    GblnValue* result = gbln_value_new_f32(value);
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewF64(
    JNIEnv* env, jobject obj, jdouble value) {
    
    GblnValue* result = gbln_value_new_f64(value);
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewBool(
    JNIEnv* env, jobject obj, jboolean value) {
    
    GblnValue* result = gbln_value_new_bool(value ? 1 : 0);
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewString(
    JNIEnv* env, jobject obj, jstring value, jint maxLen) {
    
    const char* cValue = jstringToC(env, value);
    if (!cValue) return 0;
    
    GblnValue* result = gbln_value_new_string(cValue, static_cast<size_t>(maxLen));
    
    releaseJString(env, value, cValue);
    
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewNull(
    JNIEnv* env, jobject obj) {
    
    GblnValue* result = gbln_value_new_null();
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewObject(
    JNIEnv* env, jobject obj) {
    
    GblnValue* result = gbln_value_new_object();
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewArray(
    JNIEnv* env, jobject obj) {
    
    GblnValue* result = gbln_value_new_array();
    return reinterpret_cast<jlong>(result);
}

// Value type checking functions

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI8(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_i8(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI16(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_i16(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_i32(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_i64(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU8(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_u8(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU16(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_u16(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_u32(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_u64(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsF32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_f32(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsF64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_f64(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsBool(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_bool(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsString(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_string(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsNull(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_null(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsObject(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_object(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsArray(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_is_array(value) ? JNI_TRUE : JNI_FALSE;
}

// Value getter functions

JNIEXPORT jbyte JNICALL Java_dev_gbln_FfiWrapper_valueAsI8(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jbyte>(gbln_value_as_i8(value));
}

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_valueAsI16(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jshort>(gbln_value_as_i16(value));
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_valueAsI32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jint>(gbln_value_as_i32(value));
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsI64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jlong>(gbln_value_as_i64(value));
}

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_valueAsU8(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jshort>(gbln_value_as_u8(value));
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_valueAsU16(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jint>(gbln_value_as_u16(value));
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsU32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jlong>(gbln_value_as_u32(value));
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsU64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return static_cast<jlong>(gbln_value_as_u64(value));
}

JNIEXPORT jfloat JNICALL Java_dev_gbln_FfiWrapper_valueAsF32(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_as_f32(value);
}

JNIEXPORT jdouble JNICALL Java_dev_gbln_FfiWrapper_valueAsF64(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_as_f64(value);
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueAsBool(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    return gbln_value_as_bool(value) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_valueAsString(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    const char* result = gbln_value_as_string(value);
    
    return createJString(env, result);
}

// Object operations

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_objectSet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key, jlong valuePtr) {
    
    GblnValue* object = reinterpret_cast<GblnValue*>(objectPtr);
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    
    const char* cKey = jstringToC(env, key);
    if (!cKey) return;
    
    gbln_object_set(object, cKey, value);
    
    releaseJString(env, key, cKey);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_objectGet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key) {
    
    GblnValue* object = reinterpret_cast<GblnValue*>(objectPtr);
    
    const char* cKey = jstringToC(env, key);
    if (!cKey) return 0;
    
    GblnValue* result = gbln_object_get(object, cKey);
    
    releaseJString(env, key, cKey);
    
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_objectLen(
    JNIEnv* env, jobject obj, jlong objectPtr) {
    
    GblnValue* object = reinterpret_cast<GblnValue*>(objectPtr);
    return static_cast<jint>(gbln_object_len(object));
}

JNIEXPORT jobjectArray JNICALL Java_dev_gbln_FfiWrapper_objectKeys(
    JNIEnv* env, jobject obj, jlong objectPtr) {
    
    GblnValue* object = reinterpret_cast<GblnValue*>(objectPtr);
    
    size_t len = gbln_object_len(object);
    const char** keys = gbln_object_keys(object);
    
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(len), stringClass, nullptr);
    
    for (size_t i = 0; i < len; i++) {
        jstring jKey = createJString(env, keys[i]);
        env->SetObjectArrayElement(result, static_cast<jsize>(i), jKey);
        env->DeleteLocalRef(jKey);
    }
    
    gbln_keys_free(keys, len);
    
    return result;
}

// Array operations

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_arrayPush(
    JNIEnv* env, jobject obj, jlong arrayPtr, jlong valuePtr) {
    
    GblnValue* array = reinterpret_cast<GblnValue*>(arrayPtr);
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    
    gbln_array_push(array, value);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_arrayGet(
    JNIEnv* env, jobject obj, jlong arrayPtr, jint index) {
    
    GblnValue* array = reinterpret_cast<GblnValue*>(arrayPtr);
    GblnValue* result = gbln_array_get(array, static_cast<size_t>(index));
    
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_arrayLen(
    JNIEnv* env, jobject obj, jlong arrayPtr) {
    
    GblnValue* array = reinterpret_cast<GblnValue*>(arrayPtr);
    return static_cast<jint>(gbln_array_len(array));
}

// Memory management

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_valueFree(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    gbln_value_free(value);
}

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_stringFree(
    JNIEnv* env, jobject obj, jlong stringPtr) {
    
    char* str = reinterpret_cast<char*>(stringPtr);
    gbln_string_free(str);
}

// Error handling

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_getErrorMessage(
    JNIEnv* env, jobject obj) {
    
    const char* error = gbln_get_error_message();
    return createJString(env, error);
}

// I/O operations

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_parseFile(
    JNIEnv* env, jobject obj, jstring path) {
    
    const char* cPath = jstringToC(env, path);
    if (!cPath) return 0;
    
    GblnValue* value = nullptr;
    GblnResult result = gbln_parse_file(cPath, &value);
    
    releaseJString(env, path, cPath);
    
    if (result != GBLN_OK) {
        return 0;
    }
    
    return reinterpret_cast<jlong>(value);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_writeFile(
    JNIEnv* env, jobject obj, jstring path, jlong valuePtr) {
    
    GblnValue* value = reinterpret_cast<GblnValue*>(valuePtr);
    
    const char* cPath = jstringToC(env, path);
    if (!cPath) return -1;
    
    GblnResult result = gbln_write_file(cPath, value);
    
    releaseJString(env, path, cPath);
    
    return static_cast<jint>(result);
}
