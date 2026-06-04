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

val complexityXml = layout.buildDirectory.file("reports/detekt/complexity.xml")

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektComplexityAll") {
    group = "reporting"
    description = "Coleta complexidade ciclomática de todos os métodos"
    parallel = true
    setSource(fileTree(rootDir) {
        include("**/src/main/**/*.kt")
        exclude("**/build/**")
    })
    config.setFrom(files("config/detekt/detekt-complexity.yml"))
    buildUponDefaultConfig = false
    basePath = rootDir.absolutePath
    reports {
        xml.required.set(true)
        xml.outputLocation.set(complexityXml)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
    ignoreFailures = true
}

tasks.register("complexityReport") {
    group = "reporting"
    description = "Lista métodos ordenados por complexidade ciclomática e cognitiva"
    dependsOn("detektComplexityAll")
    doLast {
        val xml = complexityXml.get().asFile
        if (!xml.exists()) { println("XML não encontrado."); return@doLast }

        data class Entry(val file: String, val line: Int, val fn: String, val mcc: Int, val cog: Int)

        val mccMap = mutableMapOf<String, Entry>()
        val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(xml)
        val fileNodes = doc.getElementsByTagName("file")

        for (i in 0 until fileNodes.length) {
            val fileNode = fileNodes.item(i) as org.w3c.dom.Element
            val path = fileNode.getAttribute("name").substringAfterLast("/")
            val errors = fileNode.getElementsByTagName("error")
            for (j in 0 until errors.length) {
                val err = errors.item(j) as org.w3c.dom.Element
                val msg = err.getAttribute("message")
                val line = err.getAttribute("line").toIntOrNull() ?: 0
                val source = err.getAttribute("source")
                val key = "$path:$line"

                val cyclomaticMatch = Regex("function (\\w+) appears to be too complex based on Cyclomatic Complexity \\(complexity: (\\d+)\\)").find(msg)
                val cognitiveMatch = Regex("function (\\w+) appears to be too complex based on Cognitive Complexity \\(complexity: (\\d+)\\)").find(msg)

                when {
                    cyclomaticMatch != null && source.contains("Cyclomatic") -> {
                        val fn = cyclomaticMatch.groupValues[1]
                        val mcc = cyclomaticMatch.groupValues[2].toInt()
                        val existing = mccMap[key]
                        mccMap[key] = Entry(path, line, fn, mcc, existing?.cog ?: 0)
                    }
                    cognitiveMatch != null && source.contains("Cognitive") -> {
                        val fn = cognitiveMatch.groupValues[1]
                        val cog = cognitiveMatch.groupValues[2].toInt()
                        val existing = mccMap[key]
                        if (existing != null) {
                            mccMap[key] = existing.copy(cog = cog)
                        } else {
                            mccMap[key] = Entry(path, line, fn, 0, cog)
                        }
                    }
                }
            }
        }

        val sorted = mccMap.values.sortedByDescending { it.mcc }
        val top = sorted.take(25)

        println("\n${"=".repeat(70)}")
        println("  Complexidade Ciclomática — Top ${top.size} de ${sorted.size} métodos")
        println("${"=".repeat(70)}")
        println("%-42s %4s %4s  %s".format("Método", "mcc", "cog", "Arquivo:Linha"))
        println("-".repeat(70))
        top.forEach {
            val label = if (it.mcc > 10) "⚠" else if (it.mcc > 5) "·" else " "
            println("$label %-40s %4d %4d  ${it.file}:${it.line}".format(it.fn, it.mcc, it.cog))
        }
        println("-".repeat(70))
        println("  ⚠ = acima de 10  · = acima de 5  (threshold do projeto: 15)")
        println("${"=".repeat(70)}\n")
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
    ":core:security",
    ":feature:chat",
    ":feature:character_details",
    ":feature:home",
    ":feature:auth"
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
        "**/*Test*.*", "**/di/**", "**/*Screen*", "**/*Activity*", "**/*Fragment*",
        "**/EncryptedPrefsStorage*"  // Android Keystore: requer device, coberto por testes instrumentados
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