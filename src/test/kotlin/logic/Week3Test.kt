package logic

import logic.World.EAST
import logic.World.WEST
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Week3Test : WorldTestBase() {
    @Test
    fun partyAgain() {
        executeGoal("partyAgain")
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertAllBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun fetchTheStars() {
        executeGoal("fetchTheStars")
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertNoBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun secureTheCave() {
        executeGoal("secureTheCave")
        for (x in 0..9) {
            val n = initialKarel.countBeepersInColumn(x)
            for (y in 0 until 10 - n) {
                assertFalse(karel.beeperAt(x, y))
            }
            for (y in 10 - n until 10) {
                assertTrue(karel.beeperAt(x, y))
            }
        }
    }

    @Test
    fun layAndRemoveTiles() {
        executeGoal("layAndRemoveTiles")
        assertKarelAt(0, 9, WEST)
        assertNoBeepers()
    }
}