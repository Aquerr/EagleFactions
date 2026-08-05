import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

val eaglefactionsId = findProperty("eaglefactions.id") as String
val eaglefactionsDescription = findProperty("eaglefactions.description") as String
val eaglefactionsVersion = findProperty("eaglefactions.version") as String
val spongeApiVersion = findProperty("sponge-api.version") as String

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.3.0"
    idea
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.8"
    id("net.kyori.blossom") version "2.1.0"
}

description = eaglefactionsDescription
group = "io.github.aquerr"
version = "$eaglefactionsVersion-API-$spongeApiVersion"

allprojects {
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
        description(eaglefactionsDescription)
        links {
            homepageLink.set(uri("https://github.com/Aquerr/EagleFactions"))
            sourceLink.set(uri("https://github.com/Aquerr/EagleFactions"))
            issuesLink.set(uri("https://github.com/Aquerr/EagleFactions/issues"))
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
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion))
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", rootProject.version.toString())
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
    maven("https://repo.mikeprimm.com/")
    maven("https://repo.bluecolored.de/releases")
}

dependencies {
    api(project(":EagleFactionsAPI"))

    implementation("com.google.guava:guava:33.4.8-jre")

    // Databases
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("com.h2database:h2:2.3.232")
    compileOnly("org.xerial:sqlite-jdbc:3.45.2.0") // Can't include it in JAR because of problematic NativeDB... //TODO: To remove...

    // Integrations
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")
    compileOnly("de.bluecolored:bluemap-api:2.7.4")
    implementation("org.bstats:bstats-sponge:3.1.0")

    // Tests
    testImplementation(project(":EagleFactionsAPI"))
    testImplementation("org.spongepowered:spongeapi:$spongeApiVersion") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    testImplementation(platform("org.junit:junit-bom:5.13.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:mariadb:1.21.3")
    testImplementation("org.testcontainers:mysql:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.xerial:sqlite-jdbc:3.45.2.0")
    testImplementation("org.apache.logging.log4j:log4j-core:2.25.1")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    jar {
        if(System.getenv("JENKINS_HOME") != null) {
            rootProject.version = version.toString() + "_" + System.getenv("BUILD_NUMBER") + "-SNAPSHOT"
            println("Version => " + rootProject.version)
        } else {
            rootProject.version = version.toString() + "-SNAPSHOT"
        }
    }

    shadowJar {

        dependsOn(test)

        mergeServiceFiles()

        val libRelocationPath = "${project.group}.${eaglefactionsId}.lib"
        relocate("org.h2", "${libRelocationPath}.db.h2")
        relocate("org.mariadb.jdbc", "${libRelocationPath}.db.mariadb")
        relocate("com.mysql", "${libRelocationPath}.db.mysql")
        relocate("com.zaxxer.hikari", "${libRelocationPath}.db.pool.hikari")
        relocate("org.bstats", "${libRelocationPath}.bstats")
        relocate("com.sun.jna", "${libRelocationPath}.sun.jna")
        relocate("com.github.benmanes", "${libRelocationPath}.github.benmanes")
        relocate("org.slf4j", "${libRelocationPath}.slf4j")
        relocate("org.apache.commons.logging", "${libRelocationPath}.apache.commons.logging")
        relocate("com.google.errorprone", "${libRelocationPath}.google.errorprone")
        relocate("com.google.protobuf", "${libRelocationPath}.google.protobuf")
        relocate("com.google.guava", "${libRelocationPath}.google.guava")
        relocate("com.google.common", "${libRelocationPath}.google.common")
        relocate("com.google.thirdparty", "${libRelocationPath}.google.thirdparty")
        relocate("com.google.j2objc.annotations", "${libRelocationPath}.google.j2objc.annotations")
        relocate("javax.annotation", "${libRelocationPath}.javax.annotation")
        relocate("org.checkerframework", "${libRelocationPath}.checkerframework")
        relocate("waffle", "${libRelocationPath}.other")

        dependencies {
            exclude("org.checkerframework")
        }

        archiveClassifier.set("")
    }

    artifacts {
        archives(shadowJar)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
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

        val jarFiles: List<String> = groovy.ant.FileNameFinder().getFileNames(project.layout.buildDirectory.get().asFile.path, "**/*.jar")

        if(jarFiles.size > 0) {
            println("Found jar files: " + jarFiles)

            var lastCommitDescription = project.extra["gitCommitDesc"]
            if(lastCommitDescription == null || lastCommitDescription == "") {
                lastCommitDescription = "No changelog provided"
            }

            project.providers.exec {
                commandLine("java", "-jar", ".." + File.separator + "jenkinsdiscordbot-1.0.jar", "EagleFactions", jarFiles[0], lastCommitDescription)
            }
        }
    }
}