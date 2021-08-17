import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.21"
    application
}

group = "bercic.mihael"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.slf4j:slf4j-nop:2.0.0-alpha1")
    implementation("edu.stanford.nlp", name = "stanford-corenlp", version = "4.2.2")
    implementation("edu.stanford.nlp", name = "stanford-corenlp", version = "4.2.2", classifier = "models")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "MainKt")
    }


    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}