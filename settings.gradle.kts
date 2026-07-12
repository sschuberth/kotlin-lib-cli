pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Gradle cannot access the version catalog from here, so hard-code the dependency.
    id("com.autonomousapps.build-health").version("3.16.1")
    id("dev.aga.gradle.version-catalog-generator").version("4.2.1")
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")

    // Required for the build-health plugin. Keep the version in sync with the one from the version catalog. See:
    // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/wiki/Adding-to-your-project
    id("org.jetbrains.kotlin.jvm").version("2.3.21").apply(false)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "kotlin-lib-cli-template"

include(":cli")
include(":lib")
