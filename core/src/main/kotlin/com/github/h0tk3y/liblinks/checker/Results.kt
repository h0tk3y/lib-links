package com.github.h0tk3y.liblinks.checker

import com.github.h0tk3y.liblinks.classes.CallInfo
import com.github.h0tk3y.liblinks.classes.MethodInfo

open class ScenarioCheckResult<T>(val scenario: Scenario<T>)

enum class MissReason { NO_SUCH_METHOD, NO_SUCH_CLASS }

class MissInfo(
    val method: MethodInfo,
    val calls: Iterable<CallInfo>,
    val reason: MissReason)

class TagFail(val tag: String, val misses: Iterable<MissInfo>)

class ScenarioFail<T>(scenario: Scenario<T>, val tagFails: List<TagFail>) : ScenarioCheckResult<T>(scenario)

class ScenarioSuccess<T>(scenario: Scenario<T>) : ScenarioCheckResult<T>(scenario)
