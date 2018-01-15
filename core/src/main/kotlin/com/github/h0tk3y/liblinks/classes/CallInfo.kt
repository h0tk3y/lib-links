package com.github.h0tk3y.liblinks.classes

data class CallInfo(
    val fromClass: String,
    val fromMethod: String,
    val methodInfo: MethodInfo,
    val source: String?,
    val lineNumber: Int?
)