package com.github.h0tk3y.liblinks.classes

fun checkCalls(calls: List<CallInfo>, lookup: Lookup): Map<MethodInfo, List<CallInfo>> {
    val callsByTarget = calls.asSequence().groupBy { it.methodInfo }
    val missingTargets = callsByTarget.keys.filterNot { lookup.hasMethod(it) }.toSet()
    return callsByTarget.filter { it.key in missingTargets }
}