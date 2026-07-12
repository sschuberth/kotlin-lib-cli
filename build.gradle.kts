val javaLanguageVersion = project.property("javaLanguageVersion") as String

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(javaLanguageVersion)
    vendor = JvmVendorSpec.ADOPTIUM
}

dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                severity("fail")

                // Exclude modules which are automatically added by the "ort-kotlin-conventions".
                exclude("org.junit.jupiter:junit-jupiter")
                exclude(libs.kotest.runner.junit5)
                exclude(libs.kotest.assertions.core)
            }

            onUsedTransitiveDependencies {
                // Ignore this rule for now as it creates a massive amount of findings.
                severity("ignore")
            }

            onIncorrectConfiguration { severity("fail") }
            onCompileOnly { severity("fail") }
            onRuntimeOnly { severity("fail") }
            onUnusedAnnotationProcessors { severity("fail") }
            onRedundantPlugins { severity("fail") }
        }
    }

    reporting {
        printBuildHealth(true)
    }

    useTypesafeProjectAccessors(true)
}

// Automatically accept the Gradle Build Scan ToS when running in CI, to allow build scans to be published.
if (System.getenv("CI") == "true") {
    extensions.findByName("develocity")?.withGroovyBuilder {
        getProperty("buildScan")?.withGroovyBuilder {
            setProperty("termsOfUseUrl", "https://gradle.com/terms-of-service")
            setProperty("termsOfUseAgree", "yes")
        }
    }
}
