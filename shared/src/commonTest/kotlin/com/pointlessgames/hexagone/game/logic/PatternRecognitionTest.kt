package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PatternRecognitionTest {

    @Test
    fun testCheckQuadrupletsWithMimics() {
        // 3 normal values + 1 mimic
        val previews1 = listOf(
            PreviewCell("1", 0, 0, 5, 0),
            PreviewCell("2", 1, 0, 5, 0),
            PreviewCell("3", 2, 0, 5, 0),
            PreviewCell("4", 3, 0, 1, 0, isMimic = true)
        )
        assertTrue(PatternRecognitionEngine.checkQuadruplets(previews1), "3 normal + 1 mimic should be a quadruplet")

        // 2 normal values + 2 mimics
        val previews2 = listOf(
            PreviewCell("1", 0, 0, 5, 0),
            PreviewCell("2", 1, 0, 5, 0),
            PreviewCell("3", 2, 0, 1, 0, isMimic = true),
            PreviewCell("4", 3, 0, 2, 0, isMimic = true)
        )
        assertTrue(PatternRecognitionEngine.checkQuadruplets(previews2), "2 normal + 2 mimics should be a quadruplet")

        // 4 mimics
        val previews3 = listOf(
            PreviewCell("1", 0, 0, 1, 0, isMimic = true),
            PreviewCell("2", 1, 0, 2, 0, isMimic = true),
            PreviewCell("3", 2, 0, 3, 0, isMimic = true),
            PreviewCell("4", 3, 0, 4, 0, isMimic = true)
        )
        assertTrue(PatternRecognitionEngine.checkQuadruplets(previews3), "4 mimics should be a quadruplet")

        // 3 normal values of different types + 1 mimic
        val previews4 = listOf(
            PreviewCell("1", 0, 0, 1, 0),
            PreviewCell("2", 1, 0, 2, 0),
            PreviewCell("3", 2, 0, 3, 0),
            PreviewCell("4", 3, 0, 4, 0, isMimic = true)
        )
        assertFalse(PatternRecognitionEngine.checkQuadruplets(previews4), "3 different + 1 mimic should NOT be a quadruplet")
    }

    @Test
    fun testPathMergeWithMimics() {
        val engine = GameEngine(5, 5)
        val grid = listOf(
            HexagonCell("1", 0, 0, 5),
            HexagonCell("2", 1, 0, 1, isMimic = true), // Mimic connecting two 5s
            HexagonCell("3", 2, 0, 5)
        )

        // Starting from (0,0) with value 5
        val (merge, _) = engine.calculatePathMerge(0, 0, 5, grid, 100)
        assertTrue(merge != null, "Path merge should succeed with mimic")
        if (merge != null) {
            assertTrue(merge.totalCells == 3, "Path merge should contain 3 cells (5, Mimic, 5)")
            assertTrue(merge.participatingIds!!.contains("2"), "Mimic should be part of the path")
        }
    }
}
