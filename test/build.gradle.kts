plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "dev.medzik.ktor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.annotations)
    ksp(projects.annotationsProcessor)

    implementation("io.ktor:ktor-client-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
}
