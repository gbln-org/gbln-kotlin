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

plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
}

group = "dev.gbln"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()

    // Set java.library.path for JNI library loading
    systemProperty("java.library.path",
        "${projectDir}/src/main/jniLibs:${System.getProperty("java.library.path")}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

// Task to build JNI library (requires C compiler and libgbln.dylib/so/dll)
tasks.register<Exec>("buildJni") {
    description = "Build JNI native library"
    group = "build"

    val osName = System.getProperty("os.name").lowercase()
    val libExtension = when {
        osName.contains("mac") || osName.contains("darwin") -> "dylib"
        osName.contains("win") -> "dll"
        else -> "so"
    }

    val outputDir = file("src/main/jniLibs")
    outputDir.mkdirs()

    val javaHome = System.getProperty("java.home")
    val includeDir = when {
        osName.contains("mac") -> "$javaHome/include"
        osName.contains("win") -> "$javaHome/include"
        else -> "$javaHome/include"
    }

    val platformInclude = when {
        osName.contains("mac") -> "$includeDir/darwin"
        osName.contains("win") -> "$includeDir/win32"
        else -> "$includeDir/linux"
    }

    commandLine = when {
        osName.contains("mac") || osName.contains("darwin") -> listOf(
            "clang++",
            "-std=c++17",
            "-shared",
            "-fPIC",
            "-I$includeDir",
            "-I$platformInclude",
            "-I../../core/c/include",
            "src/main/cpp/gbln_jni.cpp",
            "-L../../core/c/target/release",
            "-lgbln",
            "-o", "src/main/jniLibs/libgbln_jni.$libExtension"
        )
        osName.contains("win") -> listOf(
            "cl.exe",
            "/std:c++17",
            "/LD",
            "/I$includeDir",
            "/I$platformInclude",
            "/I../../core/c/include",
            "src/main/cpp/gbln_jni.cpp",
            "/link",
            "/LIBPATH:../../core/c/target/release",
            "gbln.lib",
            "/OUT:src/main/jniLibs/gbln_jni.$libExtension"
        )
        else -> listOf(
            "g++",
            "-std=c++17",
            "-shared",
            "-fPIC",
            "-I$includeDir",
            "-I$platformInclude",
            "-I../../core/c/include",
            "src/main/cpp/gbln_jni.cpp",
            "-L../../core/c/target/release",
            "-lgbln",
            "-o", "src/main/jniLibs/libgbln_jni.$libExtension"
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn("buildJni")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("GBLN Kotlin")
                description.set("Kotlin bindings for GBLN (Goblin Bounded Lean Notation)")
                url.set("https://github.com/gbln-org/gbln-kotlin")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("vvoss")
                        name.set("Vivian Voss")
                        email.set("ask@vvoss.dev")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/gbln-org/gbln-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/gbln-org/gbln-kotlin.git")
                    url.set("https://github.com/gbln-org/gbln-kotlin")
                }
            }
        }
    }
}
