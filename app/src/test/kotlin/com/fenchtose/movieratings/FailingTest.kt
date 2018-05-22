package com.fenchtose.movieratings

import org.junit.Test
import kotlin.test.assertEquals

@Suppress("IllegalIdentifier")
class FailingTest {
    @Test
    fun `this test should fail`() {
        assertEquals(true, false, "This test should fail")
    }
}