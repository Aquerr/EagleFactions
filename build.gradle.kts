import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.1"
    java
    idea
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("net.kyori.blossom") version "1.2.0"
}

allprojects {
    description = "A factions plugin that will make managing your battle-server easier. :)"
    group = "io.github.aquerr"
    version = "0.17.0"

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
        maven("https://repo.spongepowered.org/maven")
        maven("https://jitpack.io")
        maven("https://raw.github.com/FabioZumbi12/UltimateChat/mvn-repo/")
    }
}

group = "o.github.aquerr"
version = "0.17.0"

repositories {
    mavenCentral()
}

sponge {
    apiVersion("8.1.0")
    license("CHANGEME")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("eaglefactions") {
        displayName("Eagle Factions")
        entrypoint("io.github.aquerr.eaglefactions.EagleFactionsPlugin")
        description("A factions plugin that will make managing your battle-server easier. :)")
        links {
            homepage("https://github.com/Aquerr/EagleFactions")
            source("https://github.com/Aquerr/EagleFactions")
            issues("https://github.com/Aquerr/EagleFactions/issues")
        }
        contributor("Aquerr") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

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
    api(project(":EagleFactionsAPI"))
//    api("com.github.rojo8399:PlaceholderAPI:4.5.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")
    compileOnly("com.github.webbukkit:DynmapCoreAPI:v2.5")
//    api("br.net.fabiozumbi12.UltimateChat:UltimateChat-Sponge-7:1.9.1")

    testImplementation(project(":EagleFactionsAPI"))
    testImplementation("org.spongepowered:spongeapi:8.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.10.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks {
    shadowJar {
        dependsOn(test)
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("eaglefactions") {
            shadow.component(this)
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
