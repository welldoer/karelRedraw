package gui

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AutoCompleterTest {
    @Test
    fun fullSuffix() {
        val actual = completeCommand("foo() bar() baz()", "f")
        Assertions.assertEquals(listOf("oo()"), actual)
    }

    @Test
    fun partialSuffix() {
        val actual = completeCommand("foo() bar() baz()", "b")
        Assertions.assertEquals(listOf("a"), actual)
    }

    @Test
    fun ambiguous() {
        val actual = completeCommand("foo() bar() baz()", "ba")
        Assertions.assertEquals(listOf("r()", "z()"), actual)
    }

    @Test
    fun alreadyComplete() {
        val actual = completeCommand("foo() bar() baz()", "foo()")
        Assertions.assertEquals(emptyList<String>(), actual)
    }
}