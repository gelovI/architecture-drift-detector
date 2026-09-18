plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":drift-core"))
    implementation(project(":drift-analyzer-kotlin"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}