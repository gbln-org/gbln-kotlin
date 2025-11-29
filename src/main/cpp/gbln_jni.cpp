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
#include <cstring>

extern "C" {
    #include "gbln.h"
}

// Parse GBLN string
JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnParse(
    JNIEnv* env, jobject obj, jstring input, jlongArray outValue) {
    
    const char* cInput = env->GetStringUTFChars(input, nullptr);
    if (!cInput) return -1;
    
    struct GblnValue* value = nullptr;
    enum GblnErrorCode result = gbln_parse(cInput, &value);
    
    env->ReleaseStringUTFChars(input, cInput);
    
    if (result == Ok && value) {
        jlong ptr = reinterpret_cast<jlong>(value);
        env->SetLongArrayRegion(outValue, 0, 1, &ptr);
    }
    
    return static_cast<jint>(result);
}

// Free value
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_gblnValueFree(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    if (valuePtr != 0) {
        gbln_value_free(reinterpret_cast<struct GblnValue*>(valuePtr));
    }
}

// Free string
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_gblnStringFree(
    JNIEnv* env, jobject obj, jlong stringPtr) {
    
    if (stringPtr != 0) {
        free(reinterpret_cast<char*>(stringPtr));
    }
}

// Serialise to string
JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnToString(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    if (valuePtr == 0) return nullptr;
    
    char* str = gbln_to_string(reinterpret_cast<const struct GblnValue*>(valuePtr));
    if (!str) return nullptr;
    
    jstring result = env->NewStringUTF(str);
    free(str);
    return result;
}

// Serialise to pretty string
JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnToStringPretty(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    if (valuePtr == 0) return nullptr;
    
    char* str = gbln_to_string_pretty(reinterpret_cast<const struct GblnValue*>(valuePtr));
    if (!str) return nullptr;
    
    jstring result = env->NewStringUTF(str);
    free(str);
    return result;
}

// Get value type
JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueType(
    JNIEnv* env, jobject obj, jlong valuePtr) {
    
    if (valuePtr == 0) return -1;
    
    return static_cast<jint>(gbln_value_type(reinterpret_cast<const struct GblnValue*>(valuePtr)));
}

// Value getters with ok flag

JNIEXPORT jbyte JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI8(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    int8_t result = gbln_value_as_i8(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jbyte>(result);
}

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI16(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    int16_t result = gbln_value_as_i16(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jshort>(result);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    int32_t result = gbln_value_as_i32(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jint>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    int64_t result = gbln_value_as_i64(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jlong>(result);
}

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU8(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    uint8_t result = gbln_value_as_u8(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jshort>(result);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU16(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    uint16_t result = gbln_value_as_u16(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jint>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    uint32_t result = gbln_value_as_u32(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    uint64_t result = gbln_value_as_u64(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return static_cast<jlong>(result);
}

JNIEXPORT jfloat JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsF32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    float result = gbln_value_as_f32(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return result;
}

JNIEXPORT jdouble JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsF64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    double result = gbln_value_as_f64(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return result;
}

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsString(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    char* str = gbln_value_as_string(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    if (!str) return nullptr;
    
    jstring result = env->NewStringUTF(str);
    free(str);
    return result;
}

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsBool(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok) {
    
    bool cOk = false;
    bool result = gbln_value_as_bool(reinterpret_cast<const struct GblnValue*>(valuePtr), &cOk);
    
    jboolean jOk = cOk ? JNI_TRUE : JNI_FALSE;
    env->SetBooleanArrayRegion(ok, 0, 1, &jOk);
    
    return result ? JNI_TRUE : JNI_FALSE;
}

// Object operations

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnObjectGet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key) {
    
    const char* cKey = env->GetStringUTFChars(key, nullptr);
    if (!cKey) return 0;
    
    const struct GblnValue* result = gbln_object_get(
        reinterpret_cast<const struct GblnValue*>(objectPtr), cKey);
    
    env->ReleaseStringUTFChars(key, cKey);
    
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnObjectLen(
    JNIEnv* env, jobject obj, jlong objectPtr) {
    
    return static_cast<jlong>(
        gbln_object_len(reinterpret_cast<const struct GblnValue*>(objectPtr)));
}

// Array operations

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnArrayGet(
    JNIEnv* env, jobject obj, jlong arrayPtr, jlong index) {
    
    const struct GblnValue* result = gbln_array_get(
        reinterpret_cast<const struct GblnValue*>(arrayPtr),
        static_cast<uintptr_t>(index));
    
    return reinterpret_cast<jlong>(result);
}

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnArrayLen(
    JNIEnv* env, jobject obj, jlong arrayPtr) {
    
    return static_cast<jlong>(
        gbln_array_len(reinterpret_cast<const struct GblnValue*>(arrayPtr)));
}

// I/O operations

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnReadIo(
    JNIEnv* env, jobject obj, jstring path, jlongArray outValue) {
    
    const char* cPath = env->GetStringUTFChars(path, nullptr);
    if (!cPath) return -1;
    
    struct GblnValue* value = nullptr;
    enum GblnErrorCode result = gbln_read_io(cPath, &value);
    
    env->ReleaseStringUTFChars(path, cPath);
    
    if (result == Ok && value) {
        jlong ptr = reinterpret_cast<jlong>(value);
        env->SetLongArrayRegion(outValue, 0, 1, &ptr);
    }
    
    return static_cast<jint>(result);
}

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnWriteIo(
    JNIEnv* env, jobject obj, jlong valuePtr, jstring path) {
    
    const char* cPath = env->GetStringUTFChars(path, nullptr);
    if (!cPath) return -1;
    
    enum GblnErrorCode result = gbln_write_io(
        reinterpret_cast<const struct GblnValue*>(valuePtr),
        cPath,
        nullptr);  // Use default config
    
    env->ReleaseStringUTFChars(path, cPath);
    
    return static_cast<jint>(result);
}
