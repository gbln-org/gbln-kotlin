plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
}

group = "dev.gbln"
version = "0.9.0"

repositories {
    mavenCentral()
}

dependencies {
    // JNA for FFI (as specified in BINDING_BUILDS.md)
    implementation("net.java.dev.jna:jna:5.14.0")

    // Kotlin coroutines for async API
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

// Include pre-built libraries from core/ffi/libs/ in JAR
tasks.jar {
    from("../../core/ffi/libs") {
        include("**/*.so")
        include("**/*.dylib")
        include("**/*.dll")
        into("native")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("GBLN Kotlin")
                description.set("Kotlin bindings for GBLN (Goblin Bounded Lean Notation)")
                url.set("https://gbln.dev")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        name.set("Vivian Voss")
                        email.set("ask+gbln@vvoss.dev")
                    }
                }

                scm {
                    url.set("https://github.com/gbln-org/gbln-kotlin")
                }
            }
        }
    }
}
