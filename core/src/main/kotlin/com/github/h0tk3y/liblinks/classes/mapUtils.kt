package com.github.h0tk3y.liblinks.classes

@Suppress("UNCHECKED_CAST")
fun <K, V> MutableMap<K, V>.computeWhenAbsent(key: K, valueFunction: (K) -> V?) =
    (this as MutableMap<K, V?>).computeIfAbsent(key, valueFunction)