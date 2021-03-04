package gui;

public class Flexer extends freditor.Flexer {
    public static final int END = 0;
    public static final int ERROR = -1;

    public static final int NEWLINE = -2;
    public static final int FIRST_SPACE = -3;
    public static final int NEXT_SPACE = 1;

    public static final int SLASH = -4;
    public static final int SLASH_SLASH = 2;
    public static final int SLASH_STAR = 3;
    public static final int SLASH_STAR___STAR = 4;
    public static final int SLASH_STAR___STAR_SLASH = 5;

    public static final int FIRST_DIGIT = -5;
    public static final int NEXT_DIGIT = 6;

    public static final int IDENTIFIER_FIRST = -6;
    public static final int IDENTIFIER_NEXT = 7;

    // auto-generated by freditor.FlexerGenerator
    public static final int E = -7;
    public static final int EL = 8;
    public static final int ELS = 9;
    public static final int ELSE = 10;
    public static final int F = -8;
    public static final int FA = 11;
    public static final int FAL = 12;
    public static final int FALS = 13;
    public static final int FALSE = 14;
    public static final int I = -9;
    public static final int IF = 15;
    public static final int R = -10;
    public static final int RE = 16;
    public static final int REP = 17;
    public static final int REPE = 18;
    public static final int REPEA = 19;
    public static final int REPEAT = 20;
    public static final int T = -11;
    public static final int TR = 21;
    public static final int TRU = 22;
    public static final int TRUE = 23;
    public static final int V = -12;
    public static final int VO = 24;
    public static final int VOI = 25;
    public static final int VOID = 26;
    public static final int W = -13;
    public static final int WH = 27;
    public static final int WHI = 28;
    public static final int WHIL = 29;
    public static final int WHILE = 30;
    public static final int BANG = -14;
    public static final int AMPERSAND = -15;
    public static final int AMPERSAND_AMPERSAND = 31;
    public static final int OPENING_PAREN = -16;
    public static final int CLOSING_PAREN = -17;
    public static final int SEMICOLON = -18;
    public static final int OPENING_BRACE = -19;
    public static final int BAR = -20;
    public static final int BAR_BAR = 32;
    public static final int CLOSING_BRACE = -21;

    @Override
    public int openingBrace() {
        return OPENING_BRACE;
    }

    @Override
    public int closingBrace() {
        return CLOSING_BRACE;
    }

    @Override
    public int pickColorForLexeme(int endState) {
        switch (endState) {
            default:
            return 0x000000;

            case ERROR:

            case SLASH:
            case AMPERSAND:
            case BAR:
            return 0x808080;

            case SLASH_SLASH:
            case SLASH_STAR:
            case SLASH_STAR___STAR:
            case SLASH_STAR___STAR_SLASH:
            return 0x008000;

            case FIRST_DIGIT:
            case NEXT_DIGIT:
            return 0x6400c8;

            case ELSE:
            case FALSE:
            case IF:
            case REPEAT:
            case TRUE:
            case WHILE:
            return 0x0000ff;

            case VOID:
            return 0x008080;

            case OPENING_PAREN:
            case CLOSING_PAREN:
            case OPENING_BRACE:
            case CLOSING_BRACE:
            return 0xff0000;

            case BANG:
            case AMPERSAND_AMPERSAND:
            case BAR_BAR:
            return 0x804040;
        }
    }

    @Override
    protected int nextStateOrEnd(int currentState, char input) {
        switch (currentState) {
            default:
            throw new AssertionError("unhandled lexer state " + currentState + " for input " + input);
            case END:
            case ERROR:
            case NEWLINE:
            case SLASH_STAR___STAR_SLASH:
            // auto-generated by freditor.FlexerGenerator
            case BANG:
            case AMPERSAND_AMPERSAND:
            case OPENING_PAREN:
            case CLOSING_PAREN:
            case SEMICOLON:
            case OPENING_BRACE:
            case BAR_BAR:
            case CLOSING_BRACE:
            switch (input) {
                default:
                return ERROR;

                case '\n':
                return NEWLINE;
                case ' ':
                return FIRST_SPACE;
                case '/':
                return SLASH;

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                return FIRST_DIGIT;

                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 's':
                case 'u':
                case 'x':
                case 'y':
                case 'z':
                case '_':
                return IDENTIFIER_FIRST;
                // auto-generated by freditor.FlexerGenerator
                case 'e':
                return E;
                case 'f':
                return F;
                case 'i':
                return I;
                case 'r':
                return R;
                case 't':
                return T;
                case 'v':
                return V;
                case 'w':
                return W;
                case '!':
                return BANG;
                case '&':
                return AMPERSAND;
                case '(':
                return OPENING_PAREN;
                case ')':
                return CLOSING_PAREN;
                case ';':
                return SEMICOLON;
                case '{':
                return OPENING_BRACE;
                case '|':
                return BAR;
                case '}':
                return CLOSING_BRACE;
            }
            case FIRST_SPACE:
            case NEXT_SPACE:
            switch (input) {
                case ' ':
                return NEXT_SPACE;
                default:
                return END;
            }
            case SLASH:
            switch (input) {
                case '/':
                return SLASH_SLASH;
                case '*':
                return SLASH_STAR;
                default:
                return END;
            }
            case SLASH_SLASH:
            switch (input) {
                case '\n':
                return END;
                default:
                return SLASH_SLASH;
            }
            case SLASH_STAR:
            switch (input) {
                case '*':
                return SLASH_STAR___STAR;
                default:
                return SLASH_STAR;
            }
            case SLASH_STAR___STAR:
            switch (input) {
                case '*':
                return SLASH_STAR___STAR;
                case '/':
                return SLASH_STAR___STAR_SLASH;
                default:
                return SLASH_STAR;
            }
            case FIRST_DIGIT:
            case NEXT_DIGIT:
            switch (input) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                return NEXT_DIGIT;
                default:
                return END;
            }
            case IDENTIFIER_FIRST:
            case IDENTIFIER_NEXT:
            // auto-generated by freditor.FlexerGenerator
            case ELSE:
            case FALSE:
            case IF:
            case REPEAT:
            case TRUE:
            case VOID:
            case WHILE:
            return identifier(input);

            // auto-generated by freditor.FlexerGenerator
            case E:
            return keyword('l', EL, input);
            case EL:
            return keyword('s', ELS, input);
            case ELS:
            return keyword('e', ELSE, input);
            case F:
            return keyword('a', FA, input);
            case FA:
            return keyword('l', FAL, input);
            case FAL:
            return keyword('s', FALS, input);
            case FALS:
            return keyword('e', FALSE, input);
            case I:
            return keyword('f', IF, input);
            case R:
            return keyword('e', RE, input);
            case RE:
            return keyword('p', REP, input);
            case REP:
            return keyword('e', REPE, input);
            case REPE:
            return keyword('a', REPEA, input);
            case REPEA:
            return keyword('t', REPEAT, input);
            case T:
            return keyword('r', TR, input);
            case TR:
            return keyword('u', TRU, input);
            case TRU:
            return keyword('e', TRUE, input);
            case V:
            return keyword('o', VO, input);
            case VO:
            return keyword('i', VOI, input);
            case VOI:
            return keyword('d', VOID, input);
            case W:
            return keyword('h', WH, input);
            case WH:
            return keyword('i', WHI, input);
            case WHI:
            return keyword('l', WHIL, input);
            case WHIL:
            return keyword('e', WHILE, input);
            // auto-generated by freditor.FlexerGenerator
            case AMPERSAND:
            return operator('&', AMPERSAND_AMPERSAND, input);
            case BAR:
            return operator('|', BAR_BAR, input);
        }
    }

    @Override
    protected int identifier(char input) {
        switch (input) {
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case '_':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            return IDENTIFIER_NEXT;
            default:
            return END;
        }
    }
}