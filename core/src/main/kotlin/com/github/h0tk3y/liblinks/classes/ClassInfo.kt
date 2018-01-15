package com.github.h0tk3y.liblinks.classes

data class ClassInfo(
    val className: String,
    val superClassName: String?,
    val interfaceNames: List<String>,
    val methods: List<MethodInfo>)