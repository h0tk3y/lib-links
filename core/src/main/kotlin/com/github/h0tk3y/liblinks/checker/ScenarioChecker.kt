package com.github.h0tk3y.liblinks.checker

import com.github.h0tk3y.liblinks.classes.CallInfo
import com.github.h0tk3y.liblinks.classes.Lookup
import com.github.h0tk3y.liblinks.classes.LookupCache

class ScenarioChecker<T>(
    callsToCheck: Iterable<CallInfo>,
    val origins: T
) {
    private val groupedCalls = callsToCheck.groupBy { it.methodInfo }

    fun processScenario(scenario: Scenario<T>): ScenarioCheckResult<T> {
        val lookupCache = LookupCache()

        val tagFails = scenario.classpathSequence(origins).mapNotNull { (tag, classpath) ->
            val lookup = Lookup(classpath).apply { loadFromCache(lookupCache) }

            val misses = groupedCalls.mapNotNull resolve@ { (methodInfo, calls) ->
                if (lookup.getClassInfo(methodInfo.className) == null && !methodInfo.className.startsWith("java/"))
                    return@resolve MissInfo(methodInfo, calls, MissReason.NO_SUCH_CLASS)

                if (!lookup.hasMethod(methodInfo))
                    return@resolve MissInfo(methodInfo, calls, MissReason.NO_SUCH_METHOD)

                null
            }

            lookup.storeToCache(lookupCache)

            if (misses.isEmpty())
                null else
                TagFail(tag, misses)
        }.toList()

        return if (tagFails.isEmpty())
            ScenarioSuccess(scenario) else
            ScenarioFail(scenario, tagFails)
    }
}