// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt) apply false
    jacoco
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    dependencies {
        "detektPlugins"(libs.detekt.formatting)
    }
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
        autoCorrect = false
    }
}

val coveredModules = listOf(
    ":core:network",
    ":core:navigation",
    ":feature:chat",
    ":feature:character_details",
    ":feature:home"
)

tasks.register<JacocoReport>("jacocoFullReport") {
    group = "verification"
    description = "Aggregated Jacoco coverage report for all modules"

    dependsOn(coveredModules.map { "$it:testDebugUnitTest" })

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/full/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/full/jacocoFullReport.xml"))
    }

    val excludes = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "**/di/**", "**/*Screen*", "**/*Activity*", "**/*Fragment*"
    )

    classDirectories.setFrom(
        coveredModules.map { module ->
            fileTree("${project(module).layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
                exclude(excludes)
            }
        }
    )

    sourceDirectories.setFrom(
        coveredModules.flatMap { module ->
            listOf(
                "${project(module).projectDir}/src/main/java",
                "${project(module).projectDir}/src/main/kotlin"
            )
        }
    )

    executionData.setFrom(
        coveredModules.map { module ->
            "${project(module).layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec"
        }.filter { file(it).exists() }
    )
}