import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.js.translate.context.Namer.kotlin

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.2.10")
    id("java-gradle-plugin")
    id("maven-publish")
}

version = "1.0"

val kotlin_version: String by rootProject.extra

dependencies {
    compileOnly(gradleApi())
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version")
    compile(project(":core"))

    testCompile("junit:junit:4.12")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

extensions.getByType(PublishingExtension::class.java).run {
    publications.create("mavenJava", MavenPublication::class.java).apply {
        artifact(tasks.findByName("jar"))
    }
}

extensions.getByType(KotlinProjectExtension::class.java).experimental.coroutines = Coroutines.ENABLE

extensions.getByType(GradlePluginDevelopmentExtension::class.java).apply {
    plugins {
        create("libLinksPlugin") {
            id = "com.github.h0tk3y.liblinks"
            implementationClass = "com.github.h0tk3y.liblinks.LibLinksPlugin"
        }
    }
}

val install by tasks.creating(DefaultTask::class.java) {
    dependsOn("publishToMavenLocal")
}