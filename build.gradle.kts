import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.kotlin.dsl.api
import org.gradle.kotlin.dsl.runtimeClasspath
import java.io.ByteArrayOutputStream

val eaglefactionsId = findProperty("eaglefactions.id") as String
val eaglefactionsVersion = findProperty("eaglefactions.version") as String
val minecraftVersion = findProperty("minecraft.version") as String
val forgeVersion = findProperty("forge.version") as String

plugins {
    `java-library`
    java
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "5.1.+"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.kyori.blossom") version "1.3.1"
}

allprojects {
    description = "A factions mod that will make managing your battle-server easier. :)"
    group = "io.github.aquerr"
    version = "$eaglefactionsVersion-$minecraftVersion"

    tasks.withType(JavaCompile::class).configureEach {
        options.apply {
            encoding = "utf-8" // Consistent source file encoding
        }
    }

    // Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
    tasks.withType(AbstractArchiveTask::class).configureEach {
        isReproducibleFileOrder = true
        isPreserveFileTimestamps = false
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.mikeprimm.com/")
    }
}

group = "io.github.aquerr"
version = "$eaglefactionsVersion-$minecraftVersion"

repositories {
    mavenCentral()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

blossom {
    if(System.getenv("JENKINS_HOME") != null) {
        rootProject.version = version.toString() + "_" + System.getenv("BUILD_NUMBER") + "-SNAPSHOT"
        println("Version => " + rootProject.version)
    } else {
        rootProject.version = version.toString() + "-SNAPSHOT"
    }
    replaceTokenIn("src/main/java/io/github/aquerr/eaglefactions/PluginInfo.java")
    replaceToken("%VERSION%", rootProject.version.toString())
}

dependencies {
//    shadow(minecraft("net.minecraftforge:forge:${forgeVersion}") as Dependency)
    minecraft("net.minecraftforge:forge:${forgeVersion}")
    api(project(":EagleFactionsAPI"))

    compileOnly("org.mariadb.jdbc:mariadb-java-client:2.0.3")
//    implementation("com.zaxxer:HikariCP:4.0.3")
//    implementation("com.h2database:h2:2.1.214")
//    implementation("org.spongepowered:configurate-hocon:4.0.0")
    shadow("com.zaxxer:HikariCP:4.0.3")
    shadow("com.h2database:h2:2.1.214")
    shadow("org.spongepowered:configurate-hocon:4.0.0")
    compileOnly("org.xerial:sqlite-jdbc:3.39.3.0")
    compileOnly("us.dynmap:DynmapCoreAPI:3.4")
    compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:2.2.1")

    testImplementation(project(":EagleFactionsAPI"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.10.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

project.configurations {

}

project.configurations.api.configure {
    isCanBeResolved = true
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
//        configurations = listOf(project.configurations.shadow.get(), project.configurations.api.get())
        archiveClassifier.set("")

//        dependencies {
//            include(project(":EagleFactionsAPI"))
//        }

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
        create<MavenPublication>("eaglefactions")
        {
            artifactId = "eaglefactions"
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
            workingDirectory(project.file("run"))

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
            property("forge.enabledGameTestNamespaces", "eaglefactions")

//            mods.create("eaglefactions") {
//                source(sourceSets.main.get())
//            }
        }

        create("server") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            property("forge.enabledGameTestNamespaces", "eaglefactions")

            mods {
                create("eaglefactions") {
                    source(sourceSets.getByName("main"))
                }
//                create("eaglefactionsapi") {
//                    allprojects.forEach {
//                        println(it)
//                    }
//                    sources(allprojects.map {
//                        println(it)
//                        println(it.ext)
//                        it.sourceSets
//                    })
//                }
            }


//            mods.create("eaglefactions") {
//                sources(sourceSets.main.get())
//                sources(project(":EagleFactionsAPI").sourceSets.main.get())
//            }


//            mods.create("eaglefactionsapi") {
//                println(project(":EagleFactionsAPI").buildDir)
//                source(project(":EagleFactionsAPI").sourceSets.main.get())
//            }
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            property("forge.enabledGameTestNamespaces", "eaglefactions")

//            mods.create("eaglefactions") {
//                source(sourceSets.main.get())
//            }
        }

        create("data") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            args("--mod", "eaglefactions", "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))

//            mods.create("eaglefactions") {
//                source(sourceSets.main.get())
//            }
        }
    }
}

// Include resources generated by data generators.
sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}

//sourceSets {
//    main {
//        java {
//            setSrcDirs(listOf("src/main", "EagleFactionsAPI/src/main"))
//        }
//    }
//}