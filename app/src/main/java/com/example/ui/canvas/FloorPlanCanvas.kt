package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.*
import kotlin.math.*

@Composable
fun FloorPlanCanvas(
    modifier: Modifier = Modifier,
    walls: List<WallSegment>,
    portals: List<PortalItem>,
    rooms: List<RoomZone>,
    furniture: List<FurnitureItem>,
    columns: List<StructuralColumn>,
    mepItems: List<MepItem>,
    toolMode: ToolMode,
    showMepLayer: Boolean,
    selectedWallId: String?,
    selectedFurnitureId: String?,
    selectedPortalId: String?,
    zoomScale: Float,
    panOffset: Offset,
    onZoomPanChange: (Float, Offset) -> Unit,
    onSelectWall: (String?) -> Unit,
    onSelectFurniture: (String?) -> Unit,
    onSelectPortal: (String?) -> Unit,
    onWallEndpointsChange: (wallId: String, newStart: Point2D, newEnd: Point2D) -> Unit,
    onFurniturePositionChange: (furnitureId: String, newX: Float, newY: Float) -> Unit,
    onCanvasTap: (worldPos: Point2D) -> Unit,
    onSplitWallTap: (wallId: String, atDistance: Float) -> Unit
) {
    // Base scale: 1 meter = 60 pixels at zoomScale = 1.0f
    val baseMToPx = 60f
    val mToPx = baseMToPx * zoomScale

    // Function to convert meter coordinate to screen pixel
    fun worldToScreen(pt: Point2D): Offset {
        return Offset(
            pt.x * mToPx + panOffset.x,
            pt.y * mToPx + panOffset.y
        )
    }

    // Function to convert screen pixel to meter coordinate
    fun screenToWorld(px: Offset): Point2D {
        val wx = (px.x - panOffset.x) / mToPx
        val wy = (px.y - panOffset.y) / mToPx
        return Point2D(wx, wy)
    }

    // Drag tracking state
    var draggingWallEndpoint by remember { mutableStateOf<Triple<String, Boolean, Point2D>?>(null) } // wallId, isStart, originalPos
    var draggingFurnitureId by remember { mutableStateOf<String?>(null) }
    var dragStartWorld by remember { mutableStateOf<Point2D?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(toolMode, zoomScale, panOffset) {
                // Two-finger zoom & pan
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomScale * zoom).coerceIn(0.4f, 4.0f)
                    val newPan = panOffset + pan
                    onZoomPanChange(newZoom, newPan)
                }
            }
            .pointerInput(walls, furniture, portals, toolMode, zoomScale, panOffset) {
                detectTapGestures { tapOffset ->
                    val worldPt = screenToWorld(tapOffset)

                    // 1. Check Furniture click
                    val hitFurniture = furniture.find { item ->
                        val dx = worldPt.x - item.x
                        val dy = worldPt.y - item.y
                        val rad = -Math.toRadians(item.rotationDeg.toDouble())
                        val lx = dx * cos(rad) - dy * sin(rad)
                        val ly = dx * sin(rad) + dy * cos(rad)
                        abs(lx) <= item.width / 2f && abs(ly) <= item.depth / 2f
                    }

                    if (hitFurniture != null) {
                        onSelectFurniture(hitFurniture.id)
                        onSelectWall(null)
                        onSelectPortal(null)
                        return@detectTapGestures
                    }

                    // 2. Check Portals (Doors & Windows) click
                    val hitPortal = portals.find { p ->
                        val parentWall = walls.find { it.id == p.wallId }
                        if (parentWall != null) {
                            val t = (p.offsetMeters + p.width / 2f) / parentWall.length.coerceAtLeast(0.01f)
                            val center = parentWall.start.lerp(parentWall.end, t)
                            worldPt.distanceTo(center) <= (p.width / 2f + 0.25f)
                        } else false
                    }

                    if (hitPortal != null) {
                        onSelectPortal(hitPortal.id)
                        onSelectWall(hitPortal.wallId)
                        onSelectFurniture(null)
                        return@detectTapGestures
                    }

                    // 3. Check Wall click
                    var hitWall: WallSegment? = null
                    var hitWallDist = Float.MAX_VALUE

                    for (wall in walls) {
                        val len = wall.length
                        if (len == 0f) continue
                        val v = wall.end - wall.start
                        val w = worldPt - wall.start
                        val c1 = w.x * v.x + w.y * v.y
                        val c2 = v.x * v.x + v.y * v.y
                        val t = (c1 / c2).coerceIn(0f, 1f)
                        val proj = wall.start + (v * t)
                        val dist = worldPt.distanceTo(proj)

                        if (dist <= (wall.thickness / 2f + 0.30f) && dist < hitWallDist) {
                            hitWall = wall
                            hitWallDist = dist
                        }
                    }

                    if (hitWall != null) {
                        if (toolMode == ToolMode.SPLIT_WALL) {
                            val v = hitWall.end - hitWall.start
                            val w = worldPt - hitWall.start
                            val c1 = w.x * v.x + w.y * v.y
                            val c2 = v.x * v.x + v.y * v.y
                            val t = (c1 / c2).coerceIn(0.1f, 0.9f)
                            val distFromStart = t * hitWall.length
                            onSplitWallTap(hitWall.id, distFromStart)
                        } else {
                            onSelectWall(hitWall.id)
                            onSelectFurniture(null)
                            onSelectPortal(null)
                        }
                        return@detectTapGestures
                    }

                    // No element hit -> deselect & trigger canvas tap
                    onSelectWall(null)
                    onSelectFurniture(null)
                    onSelectPortal(null)
                    onCanvasTap(worldPt)
                }
            }
            .pointerInput(walls, furniture, toolMode, zoomScale, panOffset) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val worldPt = screenToWorld(startOffset)
                        dragStartWorld = worldPt

                        // Check if dragging an endpoint of selected wall
                        val selectedWall = walls.find { it.id == selectedWallId }
                        if (selectedWall != null) {
                            if (worldPt.distanceTo(selectedWall.start) <= 0.45f) {
                                draggingWallEndpoint = Triple(selectedWall.id, true, selectedWall.start)
                                return@detectDragGestures
                            } else if (worldPt.distanceTo(selectedWall.end) <= 0.45f) {
                                draggingWallEndpoint = Triple(selectedWall.id, false, selectedWall.end)
                                return@detectDragGestures
                            }
                        }

                        // Check if dragging a selected furniture
                        val selectedFurn = furniture.find { it.id == selectedFurnitureId }
                        if (selectedFurn != null && !selectedFurn.isLocked) {
                            val dx = abs(worldPt.x - selectedFurn.x)
                            val dy = abs(worldPt.y - selectedFurn.y)
                            if (dx <= selectedFurn.width / 2f + 0.2f && dy <= selectedFurn.depth / 2f + 0.2f) {
                                draggingFurnitureId = selectedFurn.id
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentWorld = screenToWorld(change.position)

                        // Drag wall endpoint
                        val ep = draggingWallEndpoint
                        if (ep != null) {
                            val wall = walls.find { it.id == ep.first }
                            if (wall != null) {
                                val snapped = currentWorld.snapTo(0.05f)
                                if (ep.second) {
                                    onWallEndpointsChange(wall.id, snapped, wall.end)
                                } else {
                                    onWallEndpointsChange(wall.id, wall.start, snapped)
                                }
                            }
                        }

                        // Drag furniture
                        val fId = draggingFurnitureId
                        if (fId != null) {
                            val furn = furniture.find { it.id == fId }
                            if (furn != null && !furn.isLocked) {
                                val snapped = currentWorld.snapTo(0.05f)
                                onFurniturePositionChange(furn.id, snapped.x, snapped.y)
                            }
                        }
                    },
                    onDragEnd = {
                        draggingWallEndpoint = null
                        draggingFurnitureId = null
                        dragStartWorld = null
                    },
                    onDragCancel = {
                        draggingWallEndpoint = null
                        draggingFurnitureId = null
                        dragStartWorld = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Architectural Grid
            drawArchitecturalGrid(panOffset, mToPx, canvasWidth, canvasHeight)

            // 2. Room polygons (Floor finishes & names)
            drawRoomZones(rooms, ::worldToScreen)

            // 3. Walls (Dual faces, bump-outs, openings, thickness)
            drawWalls(
                walls = walls,
                portals = portals,
                selectedWallId = selectedWallId,
                mToPx = mToPx,
                worldToScreen = ::worldToScreen
            )

            // 4. Doors & Windows (True physical cutouts, arcs and glass)
            drawPortals(
                portals = portals,
                walls = walls,
                selectedPortalId = selectedPortalId,
                mToPx = mToPx,
                worldToScreen = ::worldToScreen
            )

            // 5. Columns
            drawColumns(columns, ::worldToScreen, mToPx)

            // 6. Furniture items
            drawFurniture(
                furniture = furniture,
                selectedFurnitureId = selectedFurnitureId,
                mToPx = mToPx,
                worldToScreen = ::worldToScreen
            )

            // 7. MEP Layer
            if (showMepLayer) {
                drawMepLayer(mepItems, ::worldToScreen)
            }

            // 8. Handles for selected wall
            if (selectedWallId != null) {
                val wall = walls.find { it.id == selectedWallId }
                if (wall != null) {
                    drawWallHandles(wall, ::worldToScreen, mToPx)
                }
            }
        }
    }
}

private fun DrawScope.drawArchitecturalGrid(
    panOffset: Offset,
    mToPx: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val gridMeter = 0.5f // 50 cm grid line
    val stepPx = gridMeter * mToPx
    if (stepPx < 8f) return

    val startX = (panOffset.x % stepPx)
    val startY = (panOffset.y % stepPx)

    val gridColorFine = Color(0xFF94A3B8).copy(alpha = 0.12f)
    val gridColorMeter = Color(0xFF64748B).copy(alpha = 0.25f)

    var x = startX
    var i = 0
    while (x < canvasWidth) {
        val isMajor = (i % 2 == 0)
        drawLine(
            color = if (isMajor) gridColorMeter else gridColorFine,
            start = Offset(x, 0f),
            end = Offset(x, canvasHeight),
            strokeWidth = if (isMajor) 1.2f else 0.6f
        )
        x += stepPx
        i++
    }

    var y = startY
    var j = 0
    while (y < canvasHeight) {
        val isMajor = (j % 2 == 0)
        drawLine(
            color = if (isMajor) gridColorMeter else gridColorFine,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = if (isMajor) 1.2f else 0.6f
        )
        y += stepPx
        j++
    }
}

private fun DrawScope.drawRoomZones(
    rooms: List<RoomZone>,
    worldToScreen: (Point2D) -> Offset
) {
    for (room in rooms) {
        if (room.points.size < 3) continue
        val path = Path()
        val first = worldToScreen(room.points[0])
        path.moveTo(first.x, first.y)
        for (k in 1 until room.points.size) {
            val pt = worldToScreen(room.points[k])
            path.lineTo(pt.x, pt.y)
        }
        path.close()

        // Fill room floor tint
        drawPath(
            path = path,
            color = Color(room.floorColor).copy(alpha = 0.40f)
        )

        // Room perimeter dashed line
        drawPath(
            path = path,
            color = Color(0xFFCBD5E1),
            style = Stroke(
                width = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        )
    }
}

private fun DrawScope.drawWalls(
    walls: List<WallSegment>,
    portals: List<PortalItem>,
    selectedWallId: String?,
    mToPx: Float,
    worldToScreen: (Point2D) -> Offset
) {
    for (wall in walls) {
        val p1 = worldToScreen(wall.start)
        val p2 = worldToScreen(wall.end)
        val isSelected = (wall.id == selectedWallId)
        val thickPx = wall.thickness * mToPx

        val normal = wall.normal
        val normScreen = Offset(normal.x, normal.y) * (thickPx / 2f)

        // Outer face and Inner face outlines
        val faceA1 = p1 + normScreen
        val faceA2 = p2 + normScreen
        val faceB1 = p1 - normScreen
        val faceB2 = p2 - normScreen

        // Wall polygon path
        val wallPath = Path().apply {
            moveTo(faceA1.x, faceA1.y)
            lineTo(faceA2.x, faceA2.y)
            lineTo(faceB2.x, faceB2.y)
            lineTo(faceB1.x, faceB1.y)
            close()
        }

        // Draw wall base fill
        val fillColor = if (wall.isAccent) Color(wall.accentColor) else Color(wall.innerColor)
        drawPath(path = wallPath, color = fillColor)

        // Draw outer face stripe with outer color
        drawLine(
            color = Color(wall.outerColor),
            start = faceA1,
            end = faceA2,
            strokeWidth = 3f
        )
        // Draw inner face stripe with inner color
        drawLine(
            color = Color(wall.innerColor),
            start = faceB1,
            end = faceB2,
            strokeWidth = 3f
        )

        // Wall core outline
        val wallOutlineColor = if (isSelected) Color(0xFFD97706) else Color(0xFF1E293B)
        drawPath(
            path = wallPath,
            color = wallOutlineColor,
            style = Stroke(width = if (isSelected) 3.5f else 1.8f)
        )

        // If wall has a bump-out (نيش 50 سم أو تدخيل عمود), draw the recessed contour!
        if (wall.bumpOut != null) {
            val bo = wall.bumpOut
            val len = wall.length.coerceAtLeast(0.01f)
            val tStart = bo.offsetMeters / len
            val tEnd = (bo.offsetMeters + bo.widthMeters) / len

            val bStart = wall.start.lerp(wall.end, tStart)
            val bEnd = wall.start.lerp(wall.end, tEnd)

            val dirSign = if (bo.isOutward) 1f else -1f
            val depthScreen = normScreen * (bo.depthMeters / (wall.thickness / 2f).coerceAtLeast(0.01f) * dirSign)

            val s1 = worldToScreen(bStart)
            val s2 = worldToScreen(bEnd)
            val s1Recessed = s1 + depthScreen
            val s2Recessed = s2 + depthScreen

            val bumpPath = Path().apply {
                moveTo(s1.x, s1.y)
                lineTo(s1Recessed.x, s1Recessed.y)
                lineTo(s2Recessed.x, s2Recessed.y)
                lineTo(s2.x, s2.y)
            }

            drawPath(
                path = bumpPath,
                color = Color(0xFFD97706),
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            )
        }

        // Draw wall openings (شيل جزء من الحيطة: فتحات بدون باب)
        for (opening in wall.openings) {
            val len = wall.length.coerceAtLeast(0.01f)
            val tStart = opening.offsetMeters / len
            val tEnd = (opening.offsetMeters + opening.widthMeters) / len

            val opP1 = worldToScreen(wall.start.lerp(wall.end, tStart))
            val opP2 = worldToScreen(wall.start.lerp(wall.end, tEnd))

            // Draw opening void indicator (hollow dashed)
            drawLine(
                color = Color(0xFFF8FAFC),
                start = opP1,
                end = opP2,
                strokeWidth = thickPx + 2f
            )
            drawLine(
                color = Color(0xFF64748B),
                start = opP1,
                end = opP2,
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
        }

        // Dimension label along wall (e.g. 4.20 م)
        val mid = (p1 + p2) / 2f
        val offsetLabel = mid + (normScreen * 1.8f)
        // Architectural tick marks at ends
        drawLine(
            color = Color(0xFF64748B),
            start = p1 - normScreen * 0.8f,
            end = p1 + normScreen * 0.8f,
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF64748B),
            start = p2 - normScreen * 0.8f,
            end = p2 + normScreen * 0.8f,
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawPortals(
    portals: List<PortalItem>,
    walls: List<WallSegment>,
    selectedPortalId: String?,
    mToPx: Float,
    worldToScreen: (Point2D) -> Offset
) {
    for (portal in portals) {
        val wall = walls.find { it.id == portal.wallId } ?: continue
        val len = wall.length.coerceAtLeast(0.01f)
        val tStart = portal.offsetMeters / len
        val tEnd = (portal.offsetMeters + portal.width) / len

        val pStart = worldToScreen(wall.start.lerp(wall.end, tStart))
        val pEnd = worldToScreen(wall.start.lerp(wall.end, tEnd))
        val isSelected = (portal.id == selectedPortalId)

        val thickPx = wall.thickness * mToPx
        val norm = wall.normal
        val normScr = Offset(norm.x, norm.y) * (thickPx / 2f)

        // Clear the wall opening in 2D
        drawLine(
            color = Color(0xFFF8F7F4),
            start = pStart,
            end = pEnd,
            strokeWidth = thickPx + 2f
        )

        val frameColor = if (isSelected) Color(0xFFD97706) else Color(portal.frameColor)

        if (portal.type.isWindow) {
            // Window rendering: frame + glass line
            drawLine(
                color = frameColor,
                start = pStart + normScr,
                end = pEnd + normScr,
                strokeWidth = 2f
            )
            drawLine(
                color = frameColor,
                start = pStart - normScr,
                end = pEnd - normScr,
                strokeWidth = 2f
            )
            // Glass center line
            drawLine(
                color = Color(0xFF0284C7),
                start = pStart,
                end = pEnd,
                strokeWidth = 3f
            )
        } else {
            // Door rendering: jambs + swing arc!
            // Door jambs
            drawLine(
                color = frameColor,
                start = pStart - normScr,
                end = pStart + normScr,
                strokeWidth = 3f
            )
            drawLine(
                color = frameColor,
                start = pEnd - normScr,
                end = pEnd + normScr,
                strokeWidth = 3f
            )

            // Door leaf
            val doorRadius = portal.width * mToPx
            val swingNorm = when (portal.swing) {
                DoorSwing.RIGHT_IN, DoorSwing.LEFT_IN -> Offset(-norm.y, norm.x) * doorRadius
                DoorSwing.RIGHT_OUT, DoorSwing.LEFT_OUT -> Offset(norm.y, -norm.x) * doorRadius
            }

            val leafEnd = pStart + swingNorm

            // Open leaf line
            drawLine(
                color = frameColor,
                start = pStart,
                end = leafEnd,
                strokeWidth = 2.5f
            )

            // Swing Arc (quarter circle)
            drawArc(
                color = Color(0xFF94A3B8),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = pStart - Offset(doorRadius, doorRadius),
                size = Size(doorRadius * 2f, doorRadius * 2f),
                style = Stroke(
                    width = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            )
        }
    }
}

private fun DrawScope.drawColumns(
    columns: List<StructuralColumn>,
    worldToScreen: (Point2D) -> Offset,
    mToPx: Float
) {
    for (col in columns) {
        val center = worldToScreen(Point2D(col.x, col.y))
        val wPx = col.width * mToPx
        val dPx = col.depth * mToPx
        val tl = center - Offset(wPx / 2f, dPx / 2f)
        val sz = Size(wPx, dPx)

        drawRect(color = Color(0xFFE2E8F0), topLeft = tl, size = sz)
        drawRect(color = Color(0xFF334155), topLeft = tl, size = sz, style = Stroke(width = 2f))
        // Concrete hatch cross
        drawLine(
            color = Color(0xFF94A3B8),
            start = tl,
            end = tl + Offset(wPx, dPx),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFF94A3B8),
            start = tl + Offset(wPx, 0f),
            end = tl + Offset(0f, dPx),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawFurniture(
    furniture: List<FurnitureItem>,
    selectedFurnitureId: String?,
    mToPx: Float,
    worldToScreen: (Point2D) -> Offset
) {
    for (item in furniture) {
        val center = worldToScreen(Point2D(item.x, item.y))
        val isSelected = (item.id == selectedFurnitureId)
        val wPx = item.width * mToPx
        val dPx = item.depth * mToPx
        val tl = center - Offset(wPx / 2f, dPx / 2f)
        val sz = Size(wPx, dPx)

        rotate(degrees = item.rotationDeg, pivot = center) {
            // Base fill
            drawRect(
                color = Color(item.primaryColor).copy(alpha = if (item.modelKey == "rug") 0.60f else 0.90f),
                topLeft = tl,
                size = sz
            )

            // Inner styling depending on model
            when (item.modelKey) {
                "bed" -> {
                    // Pillows
                    val pillowW = wPx * 0.40f
                    val pillowD = dPx * 0.22f
                    drawRect(
                        color = Color(item.fabricColor),
                        topLeft = tl + Offset(wPx * 0.06f, dPx * 0.06f),
                        size = Size(pillowW, pillowD)
                    )
                    drawRect(
                        color = Color(item.fabricColor),
                        topLeft = tl + Offset(wPx * 0.54f, dPx * 0.06f),
                        size = Size(pillowW, pillowD)
                    )
                    // Blanket
                    drawRect(
                        color = Color(item.secondaryColor),
                        topLeft = tl + Offset(0f, dPx * 0.40f),
                        size = Size(wPx, dPx * 0.60f)
                    )
                }
                "sofa", "sofa_l" -> {
                    // Backrest & Armrests
                    drawRect(
                        color = Color(item.secondaryColor),
                        topLeft = tl,
                        size = Size(wPx, dPx * 0.28f)
                    )
                    drawRect(
                        color = Color(item.secondaryColor),
                        topLeft = tl,
                        size = Size(wPx * 0.16f, dPx)
                    )
                    drawRect(
                        color = Color(item.secondaryColor),
                        topLeft = Offset(tl.x + wPx - wPx * 0.16f, tl.y),
                        size = Size(wPx * 0.16f, dPx)
                    )
                }
                "plant" -> {
                    drawCircle(
                        color = Color(0xFF16A34A),
                        radius = min(wPx, dPx) / 2f,
                        center = center
                    )
                }
                "round" -> {
                    drawOval(
                        color = Color(item.primaryColor),
                        topLeft = tl,
                        size = sz
                    )
                }
            }

            // Outline
            val strokeColor = if (isSelected) Color(0xFFD97706) else Color(0xFF0F172A).copy(alpha = 0.5f)
            drawRect(
                color = strokeColor,
                topLeft = tl,
                size = sz,
                style = Stroke(width = if (isSelected) 3f else 1.2f)
            )

            // Orientation marker
            drawLine(
                color = if (isSelected) Color(0xFFD97706) else Color.White.copy(alpha = 0.7f),
                start = center,
                end = center + Offset(0f, -dPx * 0.42f),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawMepLayer(
    mepItems: List<MepItem>,
    worldToScreen: (Point2D) -> Offset
) {
    for (mep in mepItems) {
        val pos = worldToScreen(Point2D(mep.x, mep.y))
        drawCircle(
            color = Color(mep.type.color),
            radius = 12f,
            center = pos
        )
        drawCircle(
            color = Color.White,
            radius = 12f,
            center = pos,
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawWallHandles(
    wall: WallSegment,
    worldToScreen: (Point2D) -> Offset,
    mToPx: Float
) {
    val s = worldToScreen(wall.start)
    val e = worldToScreen(wall.end)

    // Start handle
    drawCircle(color = Color(0xFF0284C7), radius = 14f, center = s)
    drawCircle(color = Color.White, radius = 14f, center = s, style = Stroke(width = 3f))

    // End handle
    drawCircle(color = Color(0xFF0284C7), radius = 14f, center = e)
    drawCircle(color = Color.White, radius = 14f, center = e, style = Stroke(width = 3f))

    // Midpoint handle (for quick split)
    val mid = (s + e) / 2f
    drawCircle(color = Color(0xFF10B981), radius = 10f, center = mid)
    drawCircle(color = Color.White, radius = 10f, center = mid, style = Stroke(width = 2f))
}
