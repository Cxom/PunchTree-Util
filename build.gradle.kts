plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")

    implementation("net.kyori:adventure-text-minimessage:4.15.0")
}

kotlin {
    jvmToolchain(21)
}

group = "net.punchtree"
version = "1.7.0-SNAPSHOT"
description = "PunchTree-Util"

java.sourceCompatibility = JavaVersion.VERSION_21
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    test {
        useJUnitPlatform()
    }
}

val localOutputDir: String? by project
val buildLocal by tasks.registering(Copy::class) {
    group = "build"
    description = "Builds the shaded JAR locally without publishing to the live server."

    from("build/libs/${project.name}-${project.version}.jar")
    into(provider {
        if (localOutputDir != null) {
            localOutputDir?.let { project.file(it) }
        } else {
            logger.warn("Environment variable LOCAL_OUTPUT_DIR is not set. Using the default output directory.")
            project.file("build/libs")
        }
    })
    dependsOn("publishToMavenLocal")
}