plugins {
    // Apply core plugins.
    `java-library`

    // Apply precompiled script plugins.
    id("kotlin-jvm-conventions")
}

dependencies {
    implementation(libs.log4j.api.kotlin)
}
