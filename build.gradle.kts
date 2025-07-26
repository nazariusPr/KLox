plugins {
    kotlin("jvm") version "2.1.20"
    id("com.diffplug.spotless") version "6.25.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.register<JavaExec>("runGenerateAst") {
    group = "application"
    description = "Run the GenerateAst main function"
    mainClass.set("org.example.tool.GenerateAstKt")
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.example.Lox")
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.2.1")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.2.1")
    }
}
