plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":drift-core"))
    implementation(project(":drift-analyzer-kotlin"))

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.archdrift.cli.MainKt")
}

tasks.test {
    useJUnitPlatform()
}