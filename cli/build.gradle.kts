import java.nio.file.Files

import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import org.graalvm.buildtools.gradle.tasks.NativeRunTask

import org.gradle.kotlin.dsl.named

plugins {
    // Apply core plugins.
    application

    // Apply precompiled script plugins.
    id("kotlin-jvm-conventions")

    // Apply third-party plugins.
    alias(libs.plugins.graalVmNativeImage)
}

val javaLanguageVersion = project.property("javaLanguageVersion") as String

application {
    applicationName = "template"
    mainClass = "dev.schuberth.template.cli.MainKt"
}

dependencies {
    implementation(libs.clikt)
    implementation(projects.lib)
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs = buildList {
        // See https://openjdk.org/jeps/424.
        if (javaVersion.isCompatibleWith(JavaVersion.VERSION_19)) {
            add("--enable-native-access=ALL-UNNAMED")
        }
    }

    val normalizedName = name.trimEnd { !it.isLetter() }.lowercase()

    // Work around https://youtrack.jetbrains.com/issue/KTIJ-34755.
    if (normalizedName.endsWith("main") || normalizedName.endsWith("run")) {
        outputs.upToDateWhen { false }
    }
}

tasks.named<JavaExec>("run") {
    System.getenv("TERM")?.also {
        val mode = it.substringAfter('-', "16color")
        environment("FORCE_COLOR" to mode)
    }

    System.getenv("COLORTERM")?.also {
        environment("FORCE_COLOR" to it)
    }
}

tasks.named<BuildNativeImageTask>("nativeCompile") {
    doFirst {
        // Gradle's "Copy" task cannot handle symbolic links, see https://github.com/gradle/gradle/issues/3982. That is
        // why links contained in the GraalVM distribution archive get broken during provisioning and are replaced by
        // empty files. Address this by recreating the links in the toolchain directory.
        val graalvmHomeDir = System.getenv("GRAALVM_HOME")?.let { File(it) }
        val toolchainDir = graalvmHomeDir ?: run {
            val nativeImageLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(javaLanguageVersion)
                nativeImageCapable = true
            }

            options.get().javaLauncher = nativeImageLauncher

            nativeImageLauncher.get().executablePath.asFile.parentFile.run {
                if (name == "bin") parentFile else this
            }
        }

        val toolchainFiles = toolchainDir.walkTopDown().filter { it.isFile }
        val emptyFiles = toolchainFiles.filter { it.length() == 0L }

        // Find empty toolchain files that are named like other toolchain files and assume these should have been links.
        val links = toolchainFiles.mapNotNull { file ->
            emptyFiles.singleOrNull { it != file && it.name == file.name }?.let {
                file to it
            }
        }

        // Fix up symbolic links.
        links.forEach { (target, link) ->
            logger.quiet("Fixing up '$link' to link to '$target'.")

            if (link.delete()) {
                Files.createSymbolicLink(link.toPath(), target.toPath())
            } else {
                logger.warn("Unable to delete '$link'.")
            }
        }
    }
}

tasks.named<NativeRunTask>("nativeRun") {
    System.getenv("TERM")?.also {
        val mode = it.substringAfter('-', "16color")
        environment.set(mapOf("FORCE_COLOR" to mode))
    }

    System.getenv("COLORTERM")?.also {
        environment.set(mapOf("FORCE_COLOR" to it))
    }
}
