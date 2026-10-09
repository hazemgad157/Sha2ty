package com.example

import com.example.engine.FloorPlanEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testRoomPolygonArea() {
        val points = listOf(
            Point2D(0f, 0f),
            Point2D(4f, 0f),
            Point2D(4f, 5f),
            Point2D(0f, 5f)
        )
        val area = FloorPlanEngine.calculatePolygonArea(points)
        assertEquals(20.0f, area, 0.01f)
    }

    @Test
    fun testWallSplitting() {
        val wall = WallSegment(
            id = "w1",
            start = Point2D(0f, 0f),
            end = Point2D(4f, 0f),
            thickness = 0.15f
        )
        val (wallA, wallB) = FloorPlanEngine.splitWallAt(wall, 1.50f)
        assertEquals(1.50f, wallA.length, 0.05f)
        assertEquals(2.50f, wallB.length, 0.05f)
    }

    @Test
    fun testWallResizing() {
        val wall = WallSegment(
            id = "w1",
            start = Point2D(0f, 0f),
            end = Point2D(4f, 0f),
            thickness = 0.15f
        )
        val resized = FloorPlanEngine.resizeWall(wall, 3.75f)
        assertEquals(3.75f, resized.length, 0.05f)
    }

    @Test
    fun testQuantitiesCalculation() {
        val wall = WallSegment(
            id = "w1",
            start = Point2D(0f, 0f),
            end = Point2D(5f, 0f),
            thickness = 0.20f,
            height = 2.80f
        )
        val room = RoomZone(
            id = "r1",
            name = "صالون",
            points = listOf(Point2D(0f, 0f), Point2D(5f, 0f), Point2D(5f, 4f), Point2D(0f, 4f)),
            tileSizeCm = 60
        )
        val q = FloorPlanEngine.calculateQuantities(
            walls = listOf(wall),
            portals = emptyList(),
            rooms = listOf(room),
            furniture = emptyList()
        )

        assertEquals(20.0f, q.totalFloorAreaM2, 0.01f)
        assertTrue(q.paintLitersNeeded > 0)
        assertTrue(q.ceramicBoxesNeeded > 0)
    }
}
