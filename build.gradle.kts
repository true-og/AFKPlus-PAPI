plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.0.4"
    id("com.gradleup.shadow") version "8.3.6"
    eclipse
}

java { sourceCompatibility = JavaVersion.VERSION_17 }

group = "net.lapismc"

version = "1.0.6"

val apiVersion = "1.19"

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)

    inputs.properties(props)

    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://maven.lapismc.net/repository/maven/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.helpch.at/releases/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.lapismc:AFKPlus:3.4.3")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
}

tasks.build {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.shadowJar)
}

tasks.jar { archiveClassifier.set("part") }

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
    options.isFork = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}

spotless {
    java {
        removeUnusedImports()
        palantirJavaFormat()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}
