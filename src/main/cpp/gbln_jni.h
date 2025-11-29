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

#ifndef GBLN_JNI_H
#define GBLN_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Parser functions
JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_parse(
    JNIEnv* env, jobject obj, jstring input);

// Serialiser functions
JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_toString(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_toPrettyString(
    JNIEnv* env, jobject obj, jlong valuePtr);

// Value creation functions
JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI8(
    JNIEnv* env, jobject obj, jbyte value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI16(
    JNIEnv* env, jobject obj, jshort value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI32(
    JNIEnv* env, jobject obj, jint value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewI64(
    JNIEnv* env, jobject obj, jlong value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU8(
    JNIEnv* env, jobject obj, jshort value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU16(
    JNIEnv* env, jobject obj, jint value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU32(
    JNIEnv* env, jobject obj, jlong value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewU64(
    JNIEnv* env, jobject obj, jlong value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewF32(
    JNIEnv* env, jobject obj, jfloat value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewF64(
    JNIEnv* env, jobject obj, jdouble value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewBool(
    JNIEnv* env, jobject obj, jboolean value);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewString(
    JNIEnv* env, jobject obj, jstring value, jint maxLen);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewNull(
    JNIEnv* env, jobject obj);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewObject(
    JNIEnv* env, jobject obj);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueNewArray(
    JNIEnv* env, jobject obj);

// Value type checking functions
JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI8(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI16(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsI64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU8(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU16(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsU64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsF32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsF64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsBool(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsString(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsNull(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsObject(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueIsArray(
    JNIEnv* env, jobject obj, jlong valuePtr);

// Value getter functions
JNIEXPORT jbyte JNICALL Java_dev_gbln_FfiWrapper_valueAsI8(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_valueAsI16(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_valueAsI32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsI64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_valueAsU8(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_valueAsU16(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsU32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_valueAsU64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jfloat JNICALL Java_dev_gbln_FfiWrapper_valueAsF32(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jdouble JNICALL Java_dev_gbln_FfiWrapper_valueAsF64(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_valueAsBool(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_valueAsString(
    JNIEnv* env, jobject obj, jlong valuePtr);

// Object operations
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_objectSet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key, jlong valuePtr);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_objectGet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_objectLen(
    JNIEnv* env, jobject obj, jlong objectPtr);

JNIEXPORT jobjectArray JNICALL Java_dev_gbln_FfiWrapper_objectKeys(
    JNIEnv* env, jobject obj, jlong objectPtr);

// Array operations
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_arrayPush(
    JNIEnv* env, jobject obj, jlong arrayPtr, jlong valuePtr);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_arrayGet(
    JNIEnv* env, jobject obj, jlong arrayPtr, jint index);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_arrayLen(
    JNIEnv* env, jobject obj, jlong arrayPtr);

// Memory management
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_valueFree(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_stringFree(
    JNIEnv* env, jobject obj, jlong stringPtr);

// Error handling
JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_getErrorMessage(
    JNIEnv* env, jobject obj);

// I/O operations
JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_parseFile(
    JNIEnv* env, jobject obj, jstring path);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_writeFile(
    JNIEnv* env, jobject obj, jstring path, jlong valuePtr);

#ifdef __cplusplus
}
#endif

#endif // GBLN_JNI_H
