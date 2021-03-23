package parsing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue

class ParserNegativeTest {

    private fun assertDiagnostic(messageSubstring: String, sourceCode: String) {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val exception = assertThrows<Diagnostic> {
            parser.program()
        }
        assertTrue(exception.message.contains(messageSubstring))
    }

    @Test
    fun tooManyClosingBraces() {
        assertDiagnostic("Too many closing braces", """
        void main() {
          }
        }
        """)
    }

    @Test
    fun commandMissingVoid() {
        assertDiagnostic("illegal start of command", """
        main() {
        }
        """)
    }

    @Test
    fun commandMissingName() {
        assertDiagnostic("expected identifier", """
        void () {
        }
        """)
    }

    @Test
    fun commandMissingParameters() {
        assertDiagnostic("expected (", """
        void main {
        }
        """)
    }

    @Test
    fun commandMissingBody() {
        assertDiagnostic("expected {", """
        void main()
        """)
    }

    @Test
    fun nestedCommands() {
        assertDiagnostic("nested", """
        void outer() {
            void inner() {
            }
        }
        """)
    }

    @Test
    fun unclosedBlock() {
        assertDiagnostic("unclosed block", """
        void main() {
            if (onBeeper()) {
                pickBeeper();
        }
        """)
    }

    @Test
    fun numbersAreNotStatements() {
        assertDiagnostic("illegal start of statement", """
        void main() {
            123
        }
        """)
    }

    @Test
    fun commandMissingArguments() {
        assertDiagnostic("expected (", """
        void main() {
            other;
        }
        """)
    }

    @Test
    fun commandMissingSemicolon() {
        assertDiagnostic("expected ;", """
        void main() {
            other()
        }
        """)
    }

    @Test
    fun repeatMissingParens() {
        assertDiagnostic("expected (", """
        void main() {
            repeat 9 {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun repeatMissingBlock() {
        assertDiagnostic("expected {", """
        void main() {
            repeat (9)
                moveForward();
        }
        """)
    }

    @Test
    fun zeroRepetitions() {
        assertDiagnostic("0 out of range", """
        void main() {
            repeat (0) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun oneRepetition() {
        assertDiagnostic("1 out of range", """
        void main() {
            repeat (1) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun tooManyRepetitions() {
        assertDiagnostic("4096 out of range", """
        void main() {
            repeat (4096) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun integerOverflow() {
        assertDiagnostic("2147483648 out of range", """
        void main() {
            repeat (2147483648) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun ifMissingParens() {
        assertDiagnostic("expected (", """
        void main() {
            if frontIsClear() {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun ifMissingBlock() {
        assertDiagnostic("expected {", """
        void main() {
            if (frontIsClear())
                moveForward();
        }
        """)
    }

    @Test
    fun elseRequiresBlockOrIf() {
        assertDiagnostic("{ or if", """
        void main() {
            if (onBeeper()) {
                pickBeeper();
            }
            else dropBeeper();
        }
        """)
    }

    @Test
    fun whileMissingParens() {
        assertDiagnostic("expected (", """
        void main() {
            while frontIsClear() {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun whileMissingBlock() {
        assertDiagnostic("expected {", """
        void main() {
            while (frontIsClear())
                moveForward();
        }
        """)
    }

    @Test
    fun statementAsCondition() {
        assertDiagnostic("illegal start of condition", """
        void main() {
            if (turnAround()) {
            }
        }
        """)
    }

    @Test
    fun conditionMissingParens() {
        assertDiagnostic("expected (", """
        void main() {
            while (frontIsClear) {
                moveForward();
            }
        }
        """)
    }
}