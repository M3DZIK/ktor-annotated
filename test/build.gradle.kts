import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.ktor.server.netty)
    implementation(projects.annotations)
    ksp(projects.annotationsProcessor)
    kspTest(projects.annotationsProcessor)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test)
}

tasks.test {
    useJUnitPlatform()
}
