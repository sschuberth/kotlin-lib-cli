plugins {
    // Apply core plugins.
    application

    // Apply precompiled script plugins.
    id("kotlin-jvm-conventions")
}

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
