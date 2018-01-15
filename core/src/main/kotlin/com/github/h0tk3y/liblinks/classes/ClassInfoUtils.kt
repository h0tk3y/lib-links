package com.github.h0tk3y.liblinks.classes

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream

fun readClassInfo(classBytes: InputStream): ClassInfo {
    val reader = ClassReader(classBytes)
    val visitor = ClassInfoCollectingVisitor()
    reader.accept(visitor, ClassReader.SKIP_CODE)
    return visitor.run { ClassInfo(className, superClassName, interfaceNames, methods) }
}

private class ClassInfoCollectingVisitor : ClassVisitor(Opcodes.ASM5) {
    val methods = mutableListOf<MethodInfo>()
    lateinit var className: String
    var superClassName: String? = null
    val interfaceNames = mutableListOf<String>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        superClassName = superName
        interfaceNames.addAll(interfaces.orEmpty())
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        methods.add(MethodInfo(className, name, desc))
        return super.visitMethod(access, name, desc, signature, exceptions)
    }
}

fun main(args: Array<String>) {
    val input =
        File("C:\\Projects\\simpleProject\\build\\classes\\kotlin\\main\\demo\\AB.class")
            .inputStream()

    readClassInfo(input).methods.forEach(::println)
}