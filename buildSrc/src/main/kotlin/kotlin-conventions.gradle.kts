import dev.detekt.gradle.Detekt
import dev.detekt.gradle.report.ReportMergeTask

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    // Apply precompiled script plugins.
    id("base-conventions")

    // Apply third-party plugins.
    id("dev.detekt")
}

dependencies {
    detektPlugins(libsCatalog.findLibrary("plugin-detekt-formatting").get())
}

detekt {
    // Only configure differences to the default.
    buildUponDefaultConfig = true
    config = files("$rootDir/.detekt.yml")

    source = fileTree(rootDir) {
        include("*.gradle.kts")
        include("buildSrc/src/**/kotlin/**")
    } + fileTree(projectDir) {
        include("*.gradle.kts")
        include("src/**/kotlin/**")
    }

    basePath = rootDir
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors = true
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
    exclude {
        "/build/generated/" in it.file.absoluteFile.invariantSeparatorsPath
    }

    reports {
        html.required = false
        markdown.required = false

        sarif.required = true
    }

    mergeDetektReports.configure {
        input.from(this@detekt.reports.sarif.outputLocation)
    }

    finalizedBy(mergeDetektReports)
}
