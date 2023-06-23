import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.kotlin.dsl.api
import java.io.ByteArrayOutputStream

val eaglefactionsId = findProperty("eaglefactions.id") as String
val eaglefactionsVersion = findProperty("eaglefactions.version") as String
val minecraftVersion = findProperty("minecraft.version") as String
val forgeVersion = findProperty("forge.version") as String

group = "io.github.aquerr"
version = "$minecraftVersion-$eaglefactionsVersion"

plugins {
    `java-library`
    java
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "5.1.+"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.kyori.blossom") version "1.3.1"
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.mikeprimm.com/")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

blossom {
    if(System.getenv("JENKINS_HOME") != null) {
        rootProject.version = version.toString() + "_" + System.getenv("BUILD_NUMBER") + "-SNAPSHOT"
        println("Version => " + rootProject.version)
    } else {
        rootProject.version = "$version-SNAPSHOT"
    }
    replaceTokenIn("src/main/java/io/github/aquerr/eaglefactions/PluginInfo.java")
    replaceToken("%VERSION%", rootProject.version.toString())
}

configurations {
    create("shade") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }
}

val shade = configurations.getByName("shade")

project.configurations.api.configure {
    isTransitive = false
    isCanBeResolved = true
}

dependencies {
    // MC + Forge
    minecraft("net.minecraftforge:forge:${forgeVersion}")

    // EF API
    api(project(":EagleFactionsAPI"))

    // Used for dev environment only
    minecraftLibrary("com.zaxxer:HikariCP:5.0.1")
    minecraftLibrary("com.h2database:h2:2.1.214")
    minecraftLibrary("org.spongepowered:configurate-hocon:4.1.2")

    // Shading, required for final jar.
    shade("com.zaxxer:HikariCP:5.0.1")
    shade("com.h2database:h2:2.1.214")
    shade("org.spongepowered:configurate-hocon:4.1.2")

    // Compile only. Those dependencies will be provided by the server at runtime.
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("us.dynmap:DynmapCoreAPI:3.4")
    compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:2.2.1")

    // Tests
    testImplementation(project(":EagleFactionsAPI"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.10.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks {
    shadowJar {
        configurations = listOf(shade, project.configurations.api.get())
        archiveClassifier.set("")

        dependsOn(test)

        relocate("io.leangen", "io.github.aquerr.eaglefactions.lib.leangen")
        relocate("com.typesafe", "io.github.aquerr.eaglefactions.lib.typesafe")
        relocate("org.spongepowered.configurate", "io.github.aquerr.eaglefactions.lib.configurate")
        relocate("org.h2", "io.github.aquerr.eaglefactions.lib.db.h2")
        relocate("org.mariadb.jdbc", "io.github.aquerr.eaglefactions.lib.db.mariadb")
        relocate("com.zaxxer.hikari", "io.github.aquerr.eaglefactions.lib.db.pool.hikari")
        relocate("org.slf4j", "io.github.aquerr.eaglefactions.lib.slf4j")
    }

    build {
        dependsOn(shadowJar)
    }

    reobf {
        shadowJar
    }
}


tasks.register("build-release") {
    group = "build"
    description = "Task for building the release build"

    finalizedBy(tasks.shadowJar)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Aquerr/EagleFactions")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_PUBLISHING_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_PUBLISHING_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>(eaglefactionsId)
        {
            artifactId = eaglefactionsId
            description = project.description

            from(components["java"])
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

val getGitCommitDesc by tasks.registering(Exec::class) {
    commandLine("git", "log", "-1", "--pretty=%B")
    standardOutput = ByteArrayOutputStream()
    doLast {
        project.extra["gitCommitDesc"] = standardOutput.toString()
    }
}

tasks.register("printEnvironment") {
    doLast {
        System.getenv().forEach { key, value ->
            println("$key -> $value")
        }
    }
}

tasks.register("publishBuildOnDiscord") {
    dependsOn(getGitCommitDesc)
    group = "Publishing"
    description = "Task for publishing the jar file to discord's jenkins channel"
    doLast {

        val jarFiles: List<String> = groovy.ant.FileNameFinder().getFileNames(project.buildDir.path, "**/*.jar")

        if(jarFiles.size > 0) {
            println("Found jar files: " + jarFiles)

            var lastCommitDescription = project.extra["gitCommitDesc"]
            if(lastCommitDescription == null || lastCommitDescription == "") {
                lastCommitDescription = "No changelog provided"
            }

            exec {
                commandLine("java", "-jar", ".." + File.separator + "jenkinsdiscordbot-1.0.jar", "EagleFactions", jarFiles[0], lastCommitDescription)
            }
        }
    }
}

configure<UserDevExtension> {
    mappings("official", minecraftVersion)

    // accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg") // Currently, this location cannot be changed from the default.
    
    runs {
        create("client") {
            workingDirectory(project.file("run/run-client"))

            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            property("forge.logging.console.level", "debug")

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            property("forge.enabledGameTestNamespaces", eaglefactionsId)

            mods.create(eaglefactionsId) {
                source(sourceSets.main.get())
            }
        }

        create("server") {
            workingDirectory(project.file("run/run-server"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            property("forge.enabledGameTestNamespaces", eaglefactionsId)

            mods {
                create(eaglefactionsId) {
                    source(sourceSets.getByName("main").apply {
                        this.java {
                            srcDir("EagleFactionsAPI/src/main/java")
                        }
                    })
                }
            }
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            workingDirectory(project.file("run/run-test-server"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            property("forge.enabledGameTestNamespaces", "eaglefactions")

            mods.create(eaglefactionsId) {
                source(sourceSets.main.get())
            }
        }

        create("data") {
            workingDirectory(project.file("run/run-data"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            args("--mod", eaglefactionsId, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))

            mods.create(eaglefactionsId) {
                source(sourceSets.main.get())
            }
        }
    }
}

// Include resources generated by data generators.
sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}