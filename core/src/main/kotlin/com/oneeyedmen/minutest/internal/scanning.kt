package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*
import io.github.classgraph.*
import kotlin.reflect.KFunction0
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

internal data class ScannedPackageContext(
    val packageName: String,
    private val contextFuns: List<KFunction0<NodeBuilder<Unit, *>>>,
    override val properties: Map<Any, Any> = emptyMap()

) : RuntimeContext<Unit>() {
    override fun runTest(test: Test<Unit>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val parent = null
    override val name: String get() = packageName
    override val children: List<RuntimeNode> by lazy {
        contextFuns.map { f ->
            val rootWithDefaultName = f().buildRootNode()
            when (rootWithDefaultName) {
                is RuntimeContext<*> -> RuntimeContextWrapper(rootWithDefaultName,
                    name = f.name)
                is RuntimeTest -> RuntimeTestWrapper(delegate = rootWithDefaultName,
                    name = f.name)
            }
        }
    }
    
    override fun withChildren(children: List<RuntimeNode>): RuntimeContext<Unit> {
        return RuntimeContextWrapper(this, children)
    }
    
    override fun close() {
    }
}

internal fun scan(scannerConfig: ClassGraph.() -> Unit, classFilter: (ClassInfo) -> Boolean = {true}): List<ScannedPackageContext> {
    return ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .disableJarScanning()
        .disableNestedJarScanning()
        .apply(scannerConfig)
        .scan()
        .allClasses
        .filter { it.isStandardClass && !it.isAnonymousInnerClass && it.isPublic && !it.isSynthetic }
        .filter(classFilter)
        .flatMap { it.declaredMethodInfo }
        .filter { it.definesTopLevelContext() }
        .mapNotNull { it.toKotlinFunction() }
        // Check Kotlin visibility because a public static Java method might have internal visibility in Kotlin
        .filter { it.visibility == PUBLIC }
        .groupBy { it.javaMethod?.declaringClass?.`package`?.name ?: "<tests>" }
        .map { (packageName, functions) -> ScannedPackageContext(packageName, functions) }
}

private fun MethodInfo.toKotlinFunction(): KFunction0<NodeBuilder<Unit, *>>? {
    @Suppress("UNCHECKED_CAST")
    return loadClassAndGetMethod().kotlinFunction as? KFunction0<NodeBuilder<Unit, *>>
}

private fun MethodInfo.definesTopLevelContext() =
    isStatic && isPublic && parameterInfo.isEmpty() && !isBridge
        && typeSignatureOrTypeDescriptor.resultType.name() == NodeBuilder::class.java.name

private fun TypeSignature.name() =
    (this as? ClassRefTypeSignature)?.baseClassName

