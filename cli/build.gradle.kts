plugins {
    id("kotlin-conventions")

    application
}

application {
    applicationName = "template"
    mainClass = "dev.schuberth.template.cli.MainKt"
}

dependencies {
    implementation(projects.lib)

    implementation(libs.clikt)
}
