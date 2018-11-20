package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal sealed class RuntimeNode

/**
 * The runtime representation of a context.
 */
internal data class RuntimeContext<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    val children: List<RuntimeNode>,
    private val operations: Operations<PF, F>
) : ParentContext<F>, RuntimeNode(), com.oneeyedmen.minutest.Tests {

    override fun runTest(test: Test<F>) {
        parent.runTest(operations.buildParentTest(test))
    }
}

/**
 * The runtime representation of a test.
 */
internal class RuntimeTest<F>(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F
) : Test<F>, RuntimeNode(), (F)-> F by f {
    fun run() = parent.runTest(this)
}