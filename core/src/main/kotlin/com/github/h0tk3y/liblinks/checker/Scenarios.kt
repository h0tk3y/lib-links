package com.github.h0tk3y.liblinks.checker

import com.github.h0tk3y.liblinks.classes.Classpath

data class TaggedClasspath(val tag: String, val classpath: Classpath)

interface Scenario<in T> {
    fun classpathSequence(origins: T): Sequence<TaggedClasspath>
}