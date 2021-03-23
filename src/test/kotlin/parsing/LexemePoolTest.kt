package parsing

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.fail

private val keywordsx = Regex("[A-Za-z]+")

class LexemePoolTest {
    @Test
    fun keywordsComeFirst() {
        for (lexeme in lexemePool.take(NUM_KEYWORDS)) {
            assertTrue(keywordsx.matches(lexeme), lexeme)
        }
    }

    @Test
    fun firstNonKeyword() {
        val lexeme = lexemePool[NUM_KEYWORDS]
        assertFalse(keywordsx.matches(lexeme), lexeme)
    }

    @Test
    fun keywordsAreSorted() {
        for (i in 1 until NUM_KEYWORDS) {
            val a = lexemePool[i - 1]
            val b = lexemePool[i]
            if (a >= b) {
                fail("keywords are not sorted: $a >= $b")
            }
        }
    }
}