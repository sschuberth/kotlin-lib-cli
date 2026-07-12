import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    // Apply third-party plugins.
    id("com.squareup.sort-dependencies")
}

repositories {
    mavenCentral()
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
