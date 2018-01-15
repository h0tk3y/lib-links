package com.github.h0tk3y.liblinks.classes

import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

class Classpath(val files: Iterable<File>) {
    class ClassEntry(
        val innerName: String,
        val rootFile: File,
        private val inputStreamProvider: () -> InputStream
    ) {
        fun getInputStream() = inputStreamProvider()
    }

    val classes by lazy {
        files.flatMap {
            when {
                it.extension == "jar" -> classesFromJar(it)
                it.isDirectory -> classesFromDirectory(it)
                !it.exists() -> emptyList()
                else -> throw UnsupportedOperationException("Don't know how to extract classes from $it")
            }
        }.associate { it.innerName to it }
    }
}

fun classesFromJar(file: File): List<Classpath.ClassEntry> {
    val zipFile = ZipFile(file)
    val entries = zipFile.entries()
    return entries.iterator().asSequence()
        .filter { it.name.endsWith(".class") }
        .map { Classpath.ClassEntry(it.name.removeSuffix(".class"), file) { zipFile.getInputStream(it) } }
        .toList()
}

fun classesFromDirectory(directory: File): List<Classpath.ClassEntry> =
    directory.walk()
        .filter { it.extension == "class" }
        .map {
            Classpath.ClassEntry(
                it.relativeTo(directory).path.removeSuffix(".class").replace('\\', '/'),
                directory
            ) { it.inputStream() }
        }
        .toList()