package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertNull


class NullableFixtureTests : JUnit5Minutests {

    override val tests = rootContext<String?> {
        fixture {
            null
        }
        test("fixture is null") {
            assertNull(this)
        }
    }
}