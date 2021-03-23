package gui

import freditor.FlexerState
import freditor.FlexerStateBuilder
import freditor.persistent.ChampMap

import freditor.FlexerState.EMPTY
import freditor.FlexerState.THIS

object BytecodeFlexer : freditor.Flexer() {
    private val NUMBER_TAIL = FlexerState("09af", THIS)
    private val NUMBER_HEAD = NUMBER_TAIL.head()

    private val START = FlexerStateBuilder()
        .set('\n', NEWLINE)
        .set(' ', SPACE_HEAD)
        .set("09af", NUMBER_HEAD)
        .build()
        .verbatim(EMPTY, "@", "CODE", "MNEMONIC",
            "RET",
            "MOVE", "TRNL", "TRNA", "TRNR", "PICK", "DROP",
            "BEEP", "HEAD", "LCLR", "FCLR", "RCLR",
            "NOT", "AND", "OR", "XOR",
            "PUSH", "LOOP", "CALL", "JUMP", "J0MP", "J1MP")
        .setDefault(ERROR)

    override fun start(): FlexerState = START

    override fun pickColorForLexeme(previousState: FlexerState, endState: FlexerState): Int {
        val colors = if (previousState === NEWLINE) afterNewline else lexemeColors
        return colors[endState] ?: 0x000000
    }

    private val lexemeColors = ChampMap.of(ERROR, 0x808080)
        .put(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
        .put(START.read("@", "CODE", "MNEMONIC"), 0x808080)
        .put(START.read("BEEP", "HEAD", "LCLR", "FCLR", "RCLR", "NOT", "AND", "OR", "XOR", "PUSH"), 0x000080)
        .put(START.read("RET", "LOOP", "CALL", "JUMP", "J0MP", "J1MP"), 0x400000)

    private val afterNewline = lexemeColors
        .put(NUMBER_HEAD, NUMBER_TAIL, 0x808080)
}