package com.github.h0tk3y.liblinks.classes

import java.io.File

internal class LookupCache {
    val rootToClassInfo = mutableMapOf<File, List<Pair<Classpath.ClassEntry, ClassInfo>>>()
}