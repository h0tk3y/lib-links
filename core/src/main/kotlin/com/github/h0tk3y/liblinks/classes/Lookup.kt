package com.github.h0tk3y.liblinks.classes

import kotlin.coroutines.experimental.SequenceBuilder
import kotlin.coroutines.experimental.buildSequence

class Lookup(val classpath: Classpath) {
    private val classInfoCache =
        mutableMapOf<String, Pair<Classpath.ClassEntry, ClassInfo>>()

    fun getClassInfo(className: String): ClassInfo? =
        classInfoCache.computeWhenAbsent(className) {
            val classEntry = classpath.classes[className] ?: return@computeWhenAbsent null
            classEntry to classEntry.getInputStream().use { classInput -> classInput.let { readClassInfo(it) } }
        }?.second

    fun getHierarchyOfClass(className: String): List<ClassInfo> =
        buildSequence { traverseHierarchy(className) }.toList()

    fun hasMethod(calledMethod: MethodInfo): Boolean {
        if (calledMethod.className.startsWith("java/"))
            return true

        val hierarchy = getHierarchyOfClass(calledMethod.className)
        return hierarchy.any { supertype ->
            supertype.methods.any { method ->
                method.name == calledMethod.name &&
                method.descriptor == calledMethod.descriptor
            }
        }
    }

    internal fun storeToCache(lookupCache: LookupCache) {
        val classInfoGroupedByRoot = classInfoCache.values.groupBy { (entry, _) -> entry.rootFile }
        lookupCache.rootToClassInfo.putAll(classInfoGroupedByRoot)
    }

    internal fun loadFromCache(lookupCache: LookupCache) {
        val classpathFilesSet = classpath.files.toSet()
        lookupCache.rootToClassInfo
            .filterKeys { it in classpathFilesSet }
            .values.flatten()
            .forEach { entryToInfo ->
                val (entry, _) = entryToInfo
                classInfoCache.put(entry.innerName, entryToInfo)
            }
    }

    private suspend fun SequenceBuilder<ClassInfo>.traverseHierarchy(className: String) {
        val classInfo = getClassInfo(className) ?: return
        yield(classInfo)
        if (classInfo.superClassName != null)
            traverseHierarchy(classInfo.superClassName)
        for (interfaceName in classInfo.interfaceNames)
            traverseHierarchy(interfaceName)
    }
}