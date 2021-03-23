package syntax.lexer

import common.Diagnostic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class LexerNegativeTest {

    private fun assertDiagnostic(messageSubstring: String, input: String) {
        val lexer = Lexer(input)
        val exception = assertFailsWith<Diagnostic> {
            lexer.nextToken()
        }
        Assertions.assertTrue(exception.message.contains(messageSubstring))
    }

    @Test
    fun illegalCharacter() {
        assertDiagnostic(messageSubstring = "illegal character", input = "@")
    }

    @Test
    fun slashStartsComment() {
        assertDiagnostic(messageSubstring = "comments start", input = "/@")
    }

    @Test
    fun singleAmpersand() {
        assertDiagnostic(messageSubstring = "&&", input = "&")
    }

    @Test
    fun singleBar() {
        assertDiagnostic(messageSubstring = "||", input = "|")
    }
}