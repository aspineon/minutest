package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.internal.TopLevelContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.fail


class AnnotationTests : JUnit5Minutests {

    override val tests: TopLevelContextBuilder<Unit> = SKIP - rootContext {
        isNodeBuilder(MyAnnotation - test("single annotation") {})
        isNodeBuilder(MyAnnotation - context("single annotation") {})

        isNodeBuilder(MyAnnotation + AnotherAnnotation - test("2 annotations") {})
        isNodeBuilder(MyAnnotation + AnotherAnnotation + YetAnotherAnnotation - test("3 annotations") {})
        isNodeBuilder(listOf(MyAnnotation, AnotherAnnotation, YetAnotherAnnotation) - test("3 annotations") {})

        context("context") {
            annotateWith(MyAnnotation)
        }
        test("top level skip works") {
            fail("top level skip didn't work")
        }
    }
}

object MyAnnotation : TestAnnotation
object AnotherAnnotation : TestAnnotation
object YetAnotherAnnotation : TestAnnotation

// check that expression is a nodebuilder at compile time
private fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder