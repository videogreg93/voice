import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "com.hera"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://csspeechstorage.blob.core.windows.net/maven/")
    maven("https://artifact.aspose.com/repo/")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(
        group = "com.microsoft.cognitiveservices.speech",
        name = "client-sdk",
        version = "1.19.0",
        ext = "jar"
    )
    implementation(
        group = "org.apache.poi",
        name = "poi",
        version = "5.2.0"
    )
    implementation(
        group = "org.apache.poi",
        name = "poi-ooxml",
        version = "5.2.0"
    )
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            includeAllModules = true
            packageName = "voice"
            packageVersion = "0.1.0"
        }
    }
}