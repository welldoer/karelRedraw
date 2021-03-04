package vm

data class Instruction(val bytecode: Int, val position: Int) {
    
    val category: Int
        get() = bytecode.and(0xf000)

    val target: Int
        get() = bytecode.and(0x0fff)

    private val compiledFromSource: Boolean
        get() = position > 0

    fun withTarget(newTarget: Int): Instruction {
            return copy(bytecode = category.or(newTarget))
        }

    fun mapTarget(f: (Int) -> Int): Instruction {
            return withTarget(f(target))
        }

    fun shouldPause(): Boolean {
            return when (bytecode) {
                    RETURN -> compiledFromSource
                    MOVE_FORWARD, TURN_LEFT, TURN_AROUND, TURN_RIGHT, PICK_BEEPER, DROP_BEEPER -> true
                    ON_BEEPER, BEEPER_AHEAD, LEFT_IS_CLEAR, FRONT_IS_CLEAR, RIGHT_IS_CLEAR, NOT, AND, OR, XOR -> false
                    else -> compiledFromSource && (category != JUMP)
                }
        }

    fun mnemonic(): String {
            return when (bytecode) {
                    RETURN -> "RET"

                    MOVE_FORWARD -> "MOVE"
                    TURN_LEFT -> "TRNL"
                    TURN_AROUND -> "TRNA"
                    TURN_RIGHT -> "TRNR"
                    PICK_BEEPER -> "PICK"
                    DROP_BEEPER -> "DROP"

                    ON_BEEPER -> "BEEP"
                    BEEPER_AHEAD -> "HEAD"
                    LEFT_IS_CLEAR -> "LCLR"
                    FRONT_IS_CLEAR -> "FCLR"
                    RIGHT_IS_CLEAR -> "RCLR"

                    NOT -> "NOT"
                    AND -> "AND"
                    OR -> "OR"
                    XOR -> "XOR"

                    else -> when (category) {
                            PUSH -> "PUSH %03x".format(target)
                            LOOP -> "LOOP %03x".format(target)
                            CALL -> "CALL %03x".format(target)

                            JUMP -> "JUMP %03x".format(target)
                            J0MP -> "J0MP %03x".format(target)
                            J1MP -> "J1MP %03x".format(target)

                            else -> throw IllegalBytecode(bytecode)
                        }
                }
        }
}

const val RETURN = 0x0000

const val MOVE_FORWARD = 0x0001
const val TURN_LEFT = 0x0002
const val TURN_AROUND = 0x0003
const val TURN_RIGHT = 0x0004
const val PICK_BEEPER = 0x0005
const val DROP_BEEPER = 0x0006

const val ON_BEEPER = 0x0007
const val BEEPER_AHEAD = 0x0008
const val LEFT_IS_CLEAR = 0x0009
const val FRONT_IS_CLEAR = 0x000a
const val RIGHT_IS_CLEAR = 0x000b

const val NOT = 0x000c
const val AND = 0x000d
const val OR = 0x000e
const val XOR = 0x000f

const val NORM = 0x0000

const val PUSH = 0x8000
const val LOOP = 0x9000
const val CALL = 0xa000

const val JUMP = 0xb000
const val J0MP = 0xc000
const val J1MP = 0xd000

val builtinCommands = mapOf(
        "moveForward" to MOVE_FORWARD,
        "turnLeft" to TURN_LEFT,
        "turnAround" to TURN_AROUND,
        "turnRight" to TURN_RIGHT,
        "pickBeeper" to PICK_BEEPER,
        "dropBeeper" to DROP_BEEPER)

private val basicGoalInstructions = Array(XOR + 1) { Instruction(it, 0) }

fun instructionBuffer(): MutableList<Instruction> {
    return MutableList(START) { basicGoalInstructions[RETURN] }
}

fun goalInstruction(bytecode: Int): Instruction {
    return if (bytecode <= XOR) {
            basicGoalInstructions[bytecode]
        } else {
            Instruction(bytecode, 0)
        }
}