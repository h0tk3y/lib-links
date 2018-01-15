version = "1.0-SNAPSHOT"

allprojects {
    group = "com.github.h0tk3y.gradle.liblinks"
    repositories {
        jcenter()
    }
}

buildscript {
    repositories {
        jcenter()
    }
}

var kotlin_version: String by extra
kotlin_version = "1.2.10"