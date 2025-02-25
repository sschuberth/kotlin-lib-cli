import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private val Project.libs: LibrariesForLibs
    get() = extensions.getByType()

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins(libs.plugin.detekt.formatting)
}

detekt {
    // Only configure differences to the default.
    buildUponDefaultConfig = true
    config.from(files("$rootDir/.detekt.yml"))

    source.from(fileTree(".") { include("*.gradle.kts") }, "src/funTest/kotlin")

    basePath = rootDir.path
}

testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()

            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.runner.junit5)
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

val javaVersion = JavaVersion.current()
val maxKotlinJvmTarget = runCatching { JvmTarget.fromTarget(javaVersion.majorVersion) }
    .getOrDefault(enumValues<JvmTarget>().max())

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors = true
        jvmTarget = maxKotlinJvmTarget
    }
}

val mergeDetektReportsTaskName = "mergeDetektReports"
val mergeDetektReports = if (rootProject.tasks.findByName(mergeDetektReportsTaskName) != null) {
    rootProject.tasks.named<ReportMergeTask>(mergeDetektReportsTaskName)
} else {
    rootProject.tasks.register<ReportMergeTask>(mergeDetektReportsTaskName) {
        output = rootProject.layout.buildDirectory.file("reports/detekt/merged.sarif")
    }
}

tasks.withType<Detekt>().configureEach detekt@{
    jvmTarget = maxKotlinJvmTarget.target

    exclude {
        "/build/generated/" in it.file.absolutePath
    }

    reports {
        html.required = false

        // TODO: Enable this once https://github.com/detekt/detekt/issues/5034 is resolved and use the merged
        //       Markdown file as a GitHub Action job summary, see
        //       https://github.blog/2022-05-09-supercharging-github-actions-with-job-summaries/.
        md.required = false

        sarif.required = true
        txt.required = false
        xml.required = false
    }

    mergeDetektReports.configure {
        input.from(this@detekt.sarifReportFile)
    }

    finalizedBy(mergeDetektReports)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = false
        showStackTraces = false
        showStandardStreams = false
    }
}

tasks.named("check") {
    dependsOn(tasks["funTest"])
}
