package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.buildRootNode
import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import kotlin.reflect.full.memberFunctions


@Suppress("UNCHECKED_CAST")
internal fun Any.testMethods(): List<NodeBuilder<Unit, *>> = this::class.memberFunctions
    .filter { it.returnType.classifier == TopLevelContextBuilder::class }
    .map { it.call(this) as NodeBuilder<Unit, *> }

internal fun Any.rootContextFromMethods(): RuntimeContext<Unit> {
    val testMethodsAsNodes: List<NodeBuilder<Unit, *>> = testMethods()
    val singleNode = when {
        testMethodsAsNodes.isEmpty() -> error("No test methods found")
        testMethodsAsNodes.size > 1 -> error("More than one test method found")
        else -> testMethodsAsNodes.first()
    }
    return singleNode.buildRootNode() as RuntimeContext<Unit>
}
