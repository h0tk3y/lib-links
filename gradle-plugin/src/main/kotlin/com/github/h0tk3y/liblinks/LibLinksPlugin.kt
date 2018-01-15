package com.github.h0tk3y.liblinks

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UNUSED")
class LibLinksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("java") {
            val task = project.tasks.create("checkLibLinks", LibLinksTask::class.java)
        }
    }
}