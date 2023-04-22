import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

val eaglefactionsId = findProperty("eaglefactions.id") as String
val eaglefactionsVersion = findProperty("eaglefactions.version") as String
val spongeApiVersion = findProperty("sponge-api.version") as String

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
    java
    idea
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.kyori.blossom") version "1.3.1"
}

allprojects {
    description = "A factions plugin that will make managing your battle-server easier. :)"
    group = "io.github.aquerr"
    version = "$eaglefactionsVersion-API-$spongeApiVersion"

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
        maven("https://repo.mikeprimm.com/")
    }
}

group = "io.github.aquerr"
version = "$eaglefactionsVersion-API-$spongeApiVersion"

repositories {
    mavenCentral()
}

sponge {
    apiVersion(spongeApiVersion)
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(eaglefactionsId) {
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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    compileOnly("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("com.h2database:h2:2.1.214")
    compileOnly("org.xerial:sqlite-jdbc:3.39.3.0")
    compileOnly("us.dynmap:DynmapCoreAPI:3.4")
    compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:2.2.1")

    testImplementation(project(":EagleFactionsAPI"))
    testImplementation("org.spongepowered:spongeapi:$spongeApiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.10.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
}

tasks {
    shadowJar {
        dependsOn(test)

        relocate("org.h2", "io.github.aquerr.eaglefactions.lib.db.h2")
        relocate("org.mariadb.jdbc", "io.github.aquerr.eaglefactions.lib.db.mariadb")
        relocate("com.zaxxer.hikari", "io.github.aquerr.eaglefactions.lib.db.pool.hikari")
        relocate("org.slf4j", "io.github.aquerr.eaglefactions.lib.slf4j")

        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
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
