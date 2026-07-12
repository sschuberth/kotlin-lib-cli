import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import kotlin.enums.enumEntries

plugins {
    // Apply precompiled script plugins.
    id("kotlin-conventions")

    // Apply third-party plugins.
    id("com.gradleup.tapmoc")

    kotlin("jvm")
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val javaLanguageVersion = project.property("javaLanguageVersion") as String
val maxKotlinJvmTarget = runCatching { JvmTarget.fromTarget(javaLanguageVersion) }
    .getOrDefault(enumEntries<JvmTarget>().max())

tapmoc {
    java(maxKotlinJvmTarget.target.toInt())

    checkDependencies()
}

testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()

            dependencies {
                // See https://kotest.io/docs/framework/project-setup.html.
                implementation(libsCatalog.findLibrary("kotest-runner-junit5").get())

                implementation(libsCatalog.findLibrary("kotest-assertions-core").get())
            }
        }

        register<JvmTestSuite>("funTest")
    }
}

// Associate the "funTest" compilation with the "main" compilation to be able to access "internal" objects from
// functional tests.
kotlin.target.compilations.apply {
    getByName("funTest").associateWith(getByName(KotlinCompilation.MAIN_COMPILATION_NAME))
}

tasks.named("check") {
    dependsOn(tasks["funTest"])
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Build-Jdk"] = javaToolchains.compilerFor(java.toolchain).map { it.metadata.jvmVersion }
        attributes["Implementation-Version"] = version
    }
}

normalization {
    runtimeClasspath {
        metaInf {
            ignoreAttribute("Implementation-Version")
        }
    }
}
