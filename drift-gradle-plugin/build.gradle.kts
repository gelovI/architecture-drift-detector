plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":drift-core"))
    implementation(project(":drift-application"))

    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("architectureDrift") {
            id = "dev.archdrift"
            implementationClass = "dev.archdrift.gradle.ArchitectureDriftPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}