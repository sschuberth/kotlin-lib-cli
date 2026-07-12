plugins {
    // Apply precompiled script plugins.
    id("kotlin-conventions")

    // Apply third-party plugins.
    kotlin("multiplatform")
}

kotlin {
    // Always target at least the JVM in multiplatform projects.
    jvm()
}
