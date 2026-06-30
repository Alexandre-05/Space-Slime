plugins {
    java
}

group = "fr.alex96x2.slimecapture"
// version : voir gradle.properties

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveFileName.set("SlimeCapture-${project.version}.jar")
}
