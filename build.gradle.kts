import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream
import kotlin.text.StringBuilder

val ktorVersion = "2.2.1"

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.3.0"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "com.hera"
version = "0.5.1"

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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

buildConfig {
    buildConfigField("String", "SPEECH_API_KEY", "\"${getSpeechApiKey()}\"")
    buildConfigField("String", "APP_VERSION", "\"${version}\"")
    buildConfigField("Boolean", "SPEECH_ENABLED", "${getBooleanProperty("SPEECH_ENABLED", true)}")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            includeAllModules = true
            packageName = "voice"
            packageVersion = version.toString()
        }
    }
}

fun getSpeechApiKey(): String {
    val value = System.getenv("SPEECH_API_KEY")
    return if (value == null) {
        val keysFile = file("keys.properties")
        val keysProperties = Properties()
        keysProperties.load(FileInputStream(keysFile))
        keysProperties.getProperty("SPEECH_API_KEY")
    } else {
        value
    }
}

fun getBooleanProperty(key: String, defaultValue: Boolean): Boolean {
    return try {
        val keysFile = file("keys.properties")
        val keysProperties = Properties()
        keysProperties.load(FileInputStream(keysFile))
        keysProperties.getProperty(key)?.let { it.toBoolean() } ?: defaultValue
    } catch (e: Exception) {
        defaultValue
    }
}

task("regenerateMessages") {
    val messagesFile = file("src/main/resources/i18n/messages.properties")
    val enumFile = file("src/main/kotlin/i18n/Messages.kt")
    enumFile.createNewFile()
    val output = StringBuilder()
    output.append("""
        package i18n
        
        enum class Messages(val value: String) {
          
    """.trimIndent())
    val messagesProperties = Properties().apply {
        load(FileInputStream(messagesFile))
    }
    messagesProperties.propertyNames().toList().filterIsInstance<String>().forEach {
        output.append("$it(\"$it\"),\n")
    }
    output.append("}")
    enumFile.writeText(output.toString())
}