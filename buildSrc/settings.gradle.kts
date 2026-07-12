rootProject.name = "buildSrc"

plugins {
    // Gradle cannot access the version catalog from here, so hard-code the dependency.
    id("dev.panuszewski.typesafe-conventions").version("0.11.1")
}
