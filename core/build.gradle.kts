import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.js.translate.context.Namer.kotlin

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.2.10")
    id("maven")
}

version = "1.0-SNAPSHOT"

val kotlin_version: String by rootProject.extra

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version")
    compile("org.ow2.asm:asm:5.0.3")

    testCompile("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

extensions.getByType(KotlinProjectExtension::class.java).experimental.coroutines = Coroutines.ENABLE