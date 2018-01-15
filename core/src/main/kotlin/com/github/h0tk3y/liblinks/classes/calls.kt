package com.github.h0tk3y.liblinks.classes

import org.objectweb.asm.*

fun readCalls(classpath: Classpath): Set<CallInfo> {
    val methodVisitor = CallCollectingMethodVisitor()
    val classVisitor = CallCollectingClassVisitor(methodVisitor)

    classpath.classes.values.forEach { classEntry ->
        classEntry.getInputStream().use { inputStream ->
            val reader = ClassReader(inputStream)
            reader.accept(classVisitor, 0)
        }
    }

    return methodVisitor.result
}

private class CallCollectingClassVisitor(val methodVisitor: CallCollectingMethodVisitor) : ClassVisitor(Opcodes.ASM5) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        methodVisitor.currentClassName = name
        methodVisitor.currentLineNumber = null
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitSource(source: String, debug: String?) {
        methodVisitor.currentSourceFile = source
        super.visitSource(source, debug)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        methodVisitor.currentMethodName = name
        return methodVisitor
    }
}

private class CallCollectingMethodVisitor() : MethodVisitor(Opcodes.ASM5) {
    lateinit var currentClassName: String
    lateinit var currentMethodName: String
    lateinit var currentSourceFile: String
    var currentLineNumber: Int? = null

    val result = mutableSetOf<CallInfo>()

    override fun visitLineNumber(line: Int, start: Label?) {
        currentLineNumber = line
        super.visitLineNumber(line, start)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        val method = MethodInfo(owner, name, desc)
        val call = CallInfo(currentClassName, currentMethodName, method, currentSourceFile, currentLineNumber)
        result += call

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
}