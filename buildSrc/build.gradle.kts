plugins {
    // Use Kotlin DSL to write precompiled script plugins.
    `kotlin-dsl`
}

repositories {
    // Allow to resolve external plugins from precompiled script plugins.
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugin.detekt)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.sortDependencies)
    implementation(libs.plugin.tapmoc)
}
