package com.github.h0tk3y.liblinks

import com.github.h0tk3y.liblinks.checker.ScenarioChecker
import com.github.h0tk3y.liblinks.checker.ScenarioFail
import com.github.h0tk3y.liblinks.checker.ScenarioSuccess
import com.github.h0tk3y.liblinks.classes.CallInfo
import com.github.h0tk3y.liblinks.classes.Classpath
import com.github.h0tk3y.liblinks.classes.readCalls
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import org.gradle.api.tasks.Classpath as InputClasspath

open class LibLinksTask : DefaultTask() {
    init {
        group = "verification"
    }

    @get:InputClasspath
    var runtimeConfiguration = project.configurations.getByName("runtimeClasspath")

    @get:Input
    val scenariosToCheck = mutableListOf<GradleScenario>()

    @get:Input
    val includeCallsInPackages = mutableListOf<String>()
    @get:Input
    val excludeCallsInPackages = mutableListOf<String>()

    @get:Input
    val includeTargetPackages = mutableListOf<String>()
    @get:Input
    val excludeTargetPackages = mutableListOf<String>()

    @get:InputClasspath
    var classesToCheck: FileCollection = project.defaultOutputToCheck()

    @get:Input
    var failOnError = false

    @Suppress("UNUSED")
    fun checkDependency(group: String, name: String, versions: List<String>) {
        scenariosToCheck += DependencyScenario(group, name, versions)
    }

    @Suppress("UNUSED")
    fun checkFiles(name: String, excludeFiles: (File) -> Boolean, includeFilesByTag: Map<String, FileCollection>) {
        scenariosToCheck += FilesScenario(name, excludeFiles, includeFilesByTag)
    }

    @TaskAction
    @Suppress("UNUSED")
    fun checkLinks() {
        val calls = readCalls(Classpath(classesToCheck.toList()))
        val filteredCalls = filterCallsBySpecs(calls)

        val scenarioChecker = ScenarioChecker(filteredCalls, GradleContext(this, runtimeConfiguration + classesToCheck))

        var anyFailure = false

        f@ for (scenario in scenariosToCheck) {
            val result = scenarioChecker.processScenario(scenario)
            when (result) {
                is ScenarioSuccess -> continue@f
                is ScenarioFail -> {
                    reportScenarioFail(result)
                    anyFailure = true
                }
            }
        }

        if (anyFailure && failOnError)
            throw GradleException("There were unresolved usages.")
    }

    private fun reportScenarioFail(scenarioFail: ScenarioFail<GradleContext>) {
        val scenario = scenarioFail.scenario
        val warning = buildString {
            appendln("Unresolved calls for $scenario:")
            scenarioFail.tagFails.forEach { tagFail ->
                appendln("  with version ${tagFail.tag}:")
                tagFail.misses.forEach { missInfo ->
                    appendln("  * method ${missInfo.method.className.toDots()}.${missInfo.method.name}${missInfo.method.descriptor}")
                    missInfo.calls.forEach { callInfo ->
                        val location =
                            callInfo.fromClass + callInfo.fromMethod +
                            if (callInfo.source != null)
                                " (" + callInfo.source + (callInfo.lineNumber?.let { ":$it" } ?: "") + ")" else
                                ""
                        appendln("  |- called from $location")
                    }
                }
            }
        }
        logger.warn(warning)

    }

    private fun filterCallsBySpecs(calls: Iterable<CallInfo>): List<CallInfo> {
        return calls
            .filter { call ->
                includeCallsInPackages.isEmpty() ||
                includeCallsInPackages.any { p -> call.fromClass.matchesSpec(p) }
            }
            .filterNot { call ->
                excludeCallsInPackages.any { p -> call.fromClass.matchesSpec(p) }
            }
            .filter { call ->
                includeTargetPackages.isEmpty() ||
                includeTargetPackages.any { p -> call.methodInfo.className.matchesSpec(p) }
            }
            .filterNot { call ->
                excludeTargetPackages.any { p -> call.methodInfo.className.matchesSpec(p) }
            }
    }
}

private fun String.toDots() = replace("/", ".")

private fun String.matchesSpec(spec: String) =
    toDots().startsWith(spec)

private fun Project.defaultOutputToCheck(): FileCollection {
    val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
    return javaPlugin.sourceSets.getByName("main").output
}