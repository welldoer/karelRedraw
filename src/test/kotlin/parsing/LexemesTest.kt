package parsing

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import kotlin.test.fail

private val keywords = Pattern.compile("[A-Za-z]+")

class LexemesTest {
    @Test
    fun keywordsComeFirst() {
        for (lexeme in allLexemes.take(KEYWORDS)) {
            assertTrue(keywords.matcher(lexeme).matches(), lexeme)
        }
    }

    @Test
    fun firstNonKeyword() {
        val lexeme = allLexemes[KEYWORDS]
        assertFalse(keywords.matcher(lexeme).matches(), lexeme)
    }

    @Test
    fun keywordsAreSorted() {
        for (i in 1 until KEYWORDS) {
            val a = allLexemes[i - 1]
            val b = allLexemes[i]
            if (a >= b) {
                fail("keywords are not sorted: $a >= $b")
            }
        }
    }
}