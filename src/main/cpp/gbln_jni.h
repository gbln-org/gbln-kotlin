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

// Package: dev.gbln.FfiWrapper

// Parse
JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnParse(
    JNIEnv* env, jobject obj, jstring input, jlongArray outValue);

// Memory
JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_gblnValueFree(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT void JNICALL Java_dev_gbln_FfiWrapper_gblnStringFree(
    JNIEnv* env, jobject obj, jlong stringPtr);

// Serialise
JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnToString(
    JNIEnv* env, jobject obj, jlong valuePtr);

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnToStringPretty(
    JNIEnv* env, jobject obj, jlong valuePtr);

// Type query
JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueType(
    JNIEnv* env, jobject obj, jlong valuePtr);

// Value getters (with ok flag)
JNIEXPORT jbyte JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI8(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI16(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsI64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jshort JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU8(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU16(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsU64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jfloat JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsF32(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jdouble JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsF64(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jstring JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsString(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

JNIEXPORT jboolean JNICALL Java_dev_gbln_FfiWrapper_gblnValueAsBool(
    JNIEnv* env, jobject obj, jlong valuePtr, jbooleanArray ok);

// Object operations
JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnObjectGet(
    JNIEnv* env, jobject obj, jlong objectPtr, jstring key);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnObjectLen(
    JNIEnv* env, jobject obj, jlong objectPtr);

// Array operations
JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnArrayGet(
    JNIEnv* env, jobject obj, jlong arrayPtr, jlong index);

JNIEXPORT jlong JNICALL Java_dev_gbln_FfiWrapper_gblnArrayLen(
    JNIEnv* env, jobject obj, jlong arrayPtr);

// I/O
JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnReadIo(
    JNIEnv* env, jobject obj, jstring path, jlongArray outValue);

JNIEXPORT jint JNICALL Java_dev_gbln_FfiWrapper_gblnWriteIo(
    JNIEnv* env, jobject obj, jlong valuePtr, jstring path);

#ifdef __cplusplus
}
#endif

#endif // GBLN_JNI_H
