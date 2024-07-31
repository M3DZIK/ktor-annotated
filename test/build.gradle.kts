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
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")

    kspTest(projects.annotationsProcessor)
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
}

tasks.test {
    useJUnitPlatform()
}
