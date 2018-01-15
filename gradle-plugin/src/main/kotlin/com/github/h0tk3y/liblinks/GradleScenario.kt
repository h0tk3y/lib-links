package com.github.h0tk3y.liblinks

import com.github.h0tk3y.liblinks.checker.Scenario
import com.github.h0tk3y.liblinks.checker.TaggedClasspath
import com.github.h0tk3y.liblinks.classes.Classpath
import org.gradle.api.file.FileCollection
import java.io.File

data class GradleContext(val task: LibLinksTask, val originalConfiguration: FileCollection)

interface GradleScenario : Scenario<GradleContext> {
    override fun classpathSequence(origins: GradleContext): Sequence<TaggedClasspath> =
        configurationsSequence(origins.task, origins.originalConfiguration)

    fun configurationsSequence(task: LibLinksTask, originalFiles: FileCollection): Sequence<TaggedClasspath>
}

data class DependencyScenario(
    val group: String,
    val name: String,
    val versions: List<String>
) : GradleScenario {
    override fun toString() = "$group:$name"

    override fun configurationsSequence(
        task: LibLinksTask,
        originalFiles: FileCollection
    ): Sequence<TaggedClasspath> =
        versions.asSequence().map { version ->
            val configurationName = "${task.name}With${group.capitalize()}${name.capitalize()}${version.capitalize()}"
                .filter { it.isLetter() || it.isDigit() }

            val resultingConfiguration = task.project.configurations.create(configurationName).apply {
                val artifactToCheck = "$group:${this@DependencyScenario.name}:$version"

                task.project.dependencies.add(configurationName, originalFiles)
                task.project.dependencies.add(configurationName, artifactToCheck)
                resolutionStrategy { strategy ->
                    strategy.force(artifactToCheck)
                }
            }

            TaggedClasspath(version, Classpath(resultingConfiguration.resolvedConfiguration.files))
        }
}

data class FilesScenario(
    val name: String,
    val excludeFilesFromConfiguration: (File) -> Boolean,
    val taggedArtifacts: Map<String, FileCollection>
) : GradleScenario {
    override fun toString() = name

    override fun configurationsSequence(
        task: LibLinksTask,
        originalFiles: FileCollection
    ): Sequence<TaggedClasspath> =
        taggedArtifacts.asSequence().map { (tag, fileCollection) ->
            val configurationName = "${task.name}With${tag.capitalize()}".filter { it.isLetter() || it.isDigit() }

            val filteredFiles = originalFiles.files.filterNot(excludeFilesFromConfiguration)

            val resultingConfiguration = task.project.configurations.create(configurationName).apply {
                task.project.dependencies.add(configurationName, filteredFiles)
                task.project.dependencies.add(configurationName, fileCollection)
            }

            TaggedClasspath(tag, Classpath(resultingConfiguration.resolvedConfiguration.files))
        }
}