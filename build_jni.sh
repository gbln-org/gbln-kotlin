#!/bin/bash
# Build script for GBLN JNI library

set -e

# Find Java home
if [ -z "$JAVA_HOME" ]; then
    if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME=$(/usr/libexec/java_home)
    else
        echo "Error: JAVA_HOME not set and /usr/libexec/java_home not found"
        exit 1
    fi
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Detect platform
OS=$(uname -s)
ARCH=$(uname -m)

if [ "$OS" = "Darwin" ]; then
    if [ "$ARCH" = "arm64" ]; then
        PLATFORM="macos-arm64"
        LIB_EXT="dylib"
    else
        PLATFORM="macos-x64"
        LIB_EXT="dylib"
    fi
elif [ "$OS" = "Linux" ]; then
    if [ "$ARCH" = "aarch64" ]; then
        PLATFORM="linux-arm64"
        LIB_EXT="so"
    else
        PLATFORM="linux-x64"
        LIB_EXT="so"
    fi
else
    echo "Unsupported platform: $OS $ARCH"
    exit 1
fi

echo "Building for platform: $PLATFORM"

# Paths
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
GBLN_FFI_DIR="$PROJECT_ROOT/../../core/ffi"
GBLN_LIB_PATH="$GBLN_FFI_DIR/libs/$PLATFORM/libgbln.$LIB_EXT"
JNI_SRC="$PROJECT_ROOT/src/main/cpp"
BUILD_DIR="$PROJECT_ROOT/build/libs/$PLATFORM"

# Check if GBLN C library exists
if [ ! -f "$GBLN_LIB_PATH" ]; then
    echo "Error: GBLN C library not found at $GBLN_LIB_PATH"
    exit 1
fi

echo "Found GBLN library: $GBLN_LIB_PATH"

# Create build directory
mkdir -p "$BUILD_DIR"

# Compile JNI library
echo "Compiling JNI library..."

if [ "$OS" = "Darwin" ]; then
    clang++ -shared \
        -o "$BUILD_DIR/libgbln_jni.$LIB_EXT" \
        -I"$JAVA_HOME/include" \
        -I"$JAVA_HOME/include/darwin" \
        -I"$GBLN_FFI_DIR/include" \
        -L"$GBLN_FFI_DIR/libs/$PLATFORM" \
        -lgbln \
        -std=c++11 \
        -fPIC \
        "$JNI_SRC/gbln_jni.cpp"
else
    g++ -shared \
        -o "$BUILD_DIR/libgbln_jni.$LIB_EXT" \
        -I"$JAVA_HOME/include" \
        -I"$JAVA_HOME/include/linux" \
        -I"$GBLN_FFI_DIR/include" \
        -L"$GBLN_FFI_DIR/libs/$PLATFORM" \
        -lgbln \
        -std=c++11 \
        -fPIC \
        "$JNI_SRC/gbln_jni.cpp"
fi

echo "JNI library built: $BUILD_DIR/libgbln_jni.$LIB_EXT"

# Copy GBLN library to build directory (needed for runtime)
cp "$GBLN_LIB_PATH" "$BUILD_DIR/"
echo "Copied GBLN library to: $BUILD_DIR/libgbln.$LIB_EXT"

echo ""
echo "Build complete!"
echo "Libraries in: $BUILD_DIR"
