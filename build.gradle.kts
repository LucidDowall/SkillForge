plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.luciddowall"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("it.unimi.dsi:fastutil:8.5.13")
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
  options.release.set(17) // ★ 핵심: bytecode를 Java 17(major 61)로 생성
}

tasks.shadowJar {
    // Write shaded jar to a different directory to avoid Windows file locks on build/libs
    destinationDirectory.set(layout.buildDirectory.dir("shadow"))
    archiveFileName.set("SkillForge-${project.version}.jar")

    // Avoid META-INF duplicate collisions
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()

    archiveClassifier.set("")

    // Relocate bundled libs to avoid classpath conflicts
    relocate("com.google.gson", "com.luciddowall.skillforge.libs.gson")
    relocate("it.unimi.dsi.fastutil", "com.luciddowall.skillforge.libs.fastutil")
}


tasks.build {
    dependsOn(tasks.shadowJar)
}
