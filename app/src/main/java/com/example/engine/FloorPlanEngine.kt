package com.example.engine

import com.example.model.*
import java.util.UUID
import kotlin.math.*

/**
 * Bill of Quantities & Materials Estimate
 */
data class QuantitiesEstimate(
    val totalFloorAreaM2: Float,
    val totalWallNetAreaM2: Float,
    val paintLitersNeeded: Float,
    val paintCans15L: Int,
    val ceramicBoxesNeeded: Int,
    val cornicesLinearMeters: Float,
    val skirtingLinearMeters: Float,
    val furnitureTotalEgp: Double,
    val furniturePurchasedEgp: Double,
    val furnitureReservedEgp: Double,
    val furnitureIdeasEgp: Double,
    val roomEstimates: List<RoomEstimate>
)

data class RoomEstimate(
    val roomName: String,
    val areaM2: Float,
    val perimeterM: Float,
    val floorMaterial: String,
    val tileBoxes: Int
)

object FloorPlanEngine {

    /**
     * Calculates the surface area of a 2D polygon using the Shoelace formula.
     */
    fun calculatePolygonArea(points: List<Point2D>): Float {
        if (points.size < 3) return 0f
        var sum = 0f
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return abs(sum) / 2.0f
    }

    /**
     * Calculates the perimeter of a polygon in linear meters.
     */
    fun calculatePolygonPerimeter(points: List<Point2D>): Float {
        if (points.size < 2) return 0f
        var perimeter = 0f
        for (i in points.indices) {
            val j = (i + 1) % points.size
            perimeter += points[i].distanceTo(points[j])
        }
        return perimeter
    }

    /**
     * Splits a wall segment into two separate wall segments at a specific distance from the start point.
     * (تقسيم الحيطة لأجزاء بنقطة عند أي مسافة مثلاً 1.2 م من الركن)
     */
    fun splitWallAt(wall: WallSegment, distanceMeters: Float): Pair<WallSegment, WallSegment> {
        val totalLen = wall.length
        val clampedDist = distanceMeters.coerceIn(0.10f, (totalLen - 0.10f).coerceAtLeast(0.10f))
        val t = clampedDist / totalLen

        val splitPoint = wall.start.lerp(wall.end, t).snapTo(0.05f)

        val wallA = wall.copy(
            id = "w_" + UUID.randomUUID().toString().take(6),
            start = wall.start,
            end = splitPoint,
            bumpOut = if (wall.bumpOut != null && wall.bumpOut.offsetMeters < clampedDist) wall.bumpOut else null
        )

        val wallB = wall.copy(
            id = "w_" + UUID.randomUUID().toString().take(6),
            start = splitPoint,
            end = wall.end,
            bumpOut = if (wall.bumpOut != null && wall.bumpOut.offsetMeters >= clampedDist) {
                wall.bumpOut.copy(offsetMeters = wall.bumpOut.offsetMeters - clampedDist)
            } else null
        )

        return Pair(wallA, wallB)
    }

    /**
     * Resizes a wall segment to an exact specified length (e.g. 3.75 m)
     * keeping start position fixed and adjusting end position along the angle vector.
     */
    fun resizeWall(wall: WallSegment, newLengthMeters: Float): WallSegment {
        val safeLen = newLengthMeters.coerceAtLeast(0.30f)
        val angle = wall.angleRad
        val newEnd = Point2D(
            wall.start.x + cos(angle) * safeLen,
            wall.start.y + sin(angle) * safeLen
        ).snapTo(0.05f)

        return wall.copy(end = newEnd)
    }

    /**
     * Adds an indentation / bump-out to a wall (تدخيل 50 سم للنيش أو عمود مع تقفيل الجانبين تلقائياً)
     */
    fun setBumpOut(
        wall: WallSegment,
        offsetMeters: Float,
        widthMeters: Float,
        depthMeters: Float,
        isOutward: Boolean
    ): WallSegment {
        val bump = WallBumpOut(
            id = "bump_" + UUID.randomUUID().toString().take(6),
            offsetMeters = offsetMeters.coerceIn(0.10f, (wall.length - widthMeters).coerceAtLeast(0.10f)),
            widthMeters = widthMeters.coerceIn(0.30f, wall.length),
            depthMeters = depthMeters.coerceIn(0.10f, 1.20f),
            isOutward = isOutward
        )
        return wall.copy(bumpOut = bump)
    }

    /**
     * Removes bump-out from a wall.
     */
    fun removeBumpOut(wall: WallSegment): WallSegment {
        return wall.copy(bumpOut = null)
    }

    /**
     * Adds an opening / cutout to a wall (شيل جزء من الحيطة: فتحة ريسبشن / عقد / مدخل مفتوح).
     */
    fun addOpening(
        wall: WallSegment,
        offsetMeters: Float,
        widthMeters: Float,
        heightMeters: Float,
        isArched: Boolean
    ): WallSegment {
        val newOpening = WallOpening(
            id = "op_" + UUID.randomUUID().toString().take(6),
            offsetMeters = offsetMeters.coerceIn(0.05f, (wall.length - widthMeters).coerceAtLeast(0.05f)),
            widthMeters = widthMeters.coerceIn(0.40f, wall.length),
            heightMeters = heightMeters.coerceIn(0.80f, wall.height),
            isArched = isArched
        )
        return wall.copy(openings = wall.openings + newOpening)
    }

    /**
     * Computes real bill of quantities (BOQ) and price estimates.
     */
    fun calculateQuantities(
        walls: List<WallSegment>,
        portals: List<PortalItem>,
        rooms: List<RoomZone>,
        furniture: List<FurnitureItem>
    ): QuantitiesEstimate {
        var totalFloorM2 = 0f
        val roomEstimates = mutableListOf<RoomEstimate>()

        for (room in rooms) {
            val area = calculatePolygonArea(room.points)
            val perimeter = calculatePolygonPerimeter(room.points)
            totalFloorM2 += area

            // 1 box of porcelain typically covers 1.44 m2
            val coveragePerBox = when (room.tileSizeCm) {
                80 -> 1.28f
                60 -> 1.44f
                40 -> 1.20f
                else -> 1.00f
            }
            val boxes = ceil((area * 1.10f) / coveragePerBox).toInt() // 10% waste

            roomEstimates.add(
                RoomEstimate(
                    roomName = room.name,
                    areaM2 = area,
                    perimeterM = perimeter,
                    floorMaterial = room.floorMaterial.titleAr,
                    tileBoxes = boxes
                )
            )
        }

        // Calculate wall gross and net area
        var grossWallAreaM2 = 0f
        var openingsAreaM2 = 0f
        var cornicesLinearM = 0f

        for (wall in walls) {
            val wallAreaOneSide = wall.length * wall.height
            // Dual faces (inner and outer)
            grossWallAreaM2 += wallAreaOneSide * 2f
            cornicesLinearM += wall.length

            // Deduct wall openings
            for (op in wall.openings) {
                openingsAreaM2 += (op.widthMeters * op.heightMeters) * 2f
            }
        }

        // Deduct doors and windows from wall surface area
        for (portal in portals) {
            openingsAreaM2 += (portal.width * portal.height) * 2f
        }

        val netWallAreaM2 = max(0f, grossWallAreaM2 - openingsAreaM2)

        // 1 liter of paint covers ~10 m2 (two coats)
        val paintLiters = netWallAreaM2 / 10f
        val paintCans15L = ceil(paintLiters / 15f).toInt()

        val totalCeramicBoxes = roomEstimates.sumOf { it.tileBoxes }

        // Furniture costs
        var fTotal = 0.0
        var fPurchased = 0.0
        var fReserved = 0.0
        var fIdeas = 0.0

        for (item in furniture) {
            fTotal += item.priceEgp
            when (item.purchaseStatus) {
                PurchaseStatus.PURCHASED -> fPurchased += item.priceEgp
                PurchaseStatus.RESERVED -> fReserved += item.priceEgp
                PurchaseStatus.IDEA -> fIdeas += item.priceEgp
            }
        }

        return QuantitiesEstimate(
            totalFloorAreaM2 = totalFloorM2,
            totalWallNetAreaM2 = netWallAreaM2,
            paintLitersNeeded = paintLiters,
            paintCans15L = max(1, paintCans15L),
            ceramicBoxesNeeded = totalCeramicBoxes,
            cornicesLinearMeters = cornicesLinearM,
            skirtingLinearMeters = cornicesLinearM,
            furnitureTotalEgp = fTotal,
            furniturePurchasedEgp = fPurchased,
            furnitureReservedEgp = fReserved,
            furnitureIdeasEgp = fIdeas,
            roomEstimates = roomEstimates
        )
    }
}
