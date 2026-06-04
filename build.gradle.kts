// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.roborazzi) apply false
    jacoco
}

dependencies {
    "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    }
}

allprojects {
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }
    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
    group = "verification"
    description = "Relatório Detekt agregado de todos os módulos"
    parallel = true
    setSource(fileTree(rootDir) {
        include("**/src/**/*.kt")
        exclude("**/build/**", "**/test/**", "**/androidTest/**")
    })
    config.setFrom(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    basePath = rootDir.absolutePath
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt-all.html"))
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

tasks.register("installGitHooks") {
    group = "setup"
    description = "Configura git para usar os hooks em .githooks/"
    doLast {
        ProcessBuilder("git", "config", "core.hooksPath", ".githooks")
            .directory(rootDir)
            .start()
            .waitFor()
        println("Git hooks instalados. Pre-commit Detekt ativo.")
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