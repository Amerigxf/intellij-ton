import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.time.Clock
import java.time.Instant

plugins {
    kotlin("jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.3"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

if (properties("pluginSnapshot").toBooleanLenient() == true) {
    val time = Instant.now(Clock.systemUTC())
    val formattedTime = time.toString().substring(2, 16).replace("[-T:]".toRegex(), "")
    version = "$version-SNAPSHOT+$formattedTime"
    println("version: $version")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test-junit"))
}

sourceSets["main"].java.srcDirs("src/main/gen")

intellij {
    version.set("LATEST-EAP-SNAPSHOT")
    updateSinceUntilBuild.set(false)
}

changelog {
    version.set(version)
    path.set("${project.projectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}]" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        sinceBuild.set("212")
        changeNotes.set(provider {
            changelog.run {
                getLatest()
            }.toHTML()
        })
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useJUnitPlatform()
    }
}

fun properties(key: String) = project.findProperty(key).toString()
