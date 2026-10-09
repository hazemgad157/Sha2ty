package com.example.ui.threed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.*

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(s: Float) = Vector3(x * s, y * s, z * s)

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun normalize(): Vector3 {
        val l = length().coerceAtLeast(0.0001f)
        return Vector3(x / l, y / l, z / l)
    }
}

data class Poly3D(
    val vertices: List<Vector3>,
    val baseColor: Color,
    val normal: Vector3,
    var depth: Float = 0f
)

@Composable
fun Apartment3DViewer(
    modifier: Modifier = Modifier,
    walls: List<WallSegment>,
    portals: List<PortalItem>,
    rooms: List<RoomZone>,
    furniture: List<FurnitureItem>,
    columns: List<StructuralColumn>
) {
    // 3D Camera & Lighting State
    var yawDeg by remember { mutableFloatStateOf(45f) }
    var pitchDeg by remember { mutableFloatStateOf(35f) }
    var cameraDist by remember { mutableFloatStateOf(16f) }
    var cameraPanX by remember { mutableFloatStateOf(5.5f) }
    var cameraPanZ by remember { mutableFloatStateOf(4.5f) }

    // Viewing modes
    var isWalkthroughMode by remember { mutableStateOf(false) } // وضع المشي جوة الشقة
    var isNightMode by remember { mutableStateOf(false) }       // وضع ليلي
    var isCutawayMode by remember { mutableStateOf(true) }      // قص الحيطان لسهولة الرؤية

    // Walkthrough eye position
    var walkX by remember { mutableFloatStateOf(3.5f) }
    var walkZ by remember { mutableFloatStateOf(3.5f) }
    var walkYawDeg by remember { mutableFloatStateOf(90f) }

    val wallDisplayHeight = if (isCutawayMode && !isWalkthroughMode) 1.25f else 2.80f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF0F141C) else Color(0xFFECE7DE))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isWalkthroughMode) {
                    if (!isWalkthroughMode) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            yawDeg = (yawDeg + dragAmount.x * 0.45f) % 360f
                            pitchDeg = (pitchDeg - dragAmount.y * 0.35f).coerceIn(10f, 85f)
                        }
                    } else {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            walkYawDeg = (walkYawDeg + dragAmount.x * 0.4f) % 360f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (!isWalkthroughMode) {
                            cameraDist = (cameraDist / zoom).coerceIn(6f, 35f)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Camera setup
            val camPos = if (!isWalkthroughMode) {
                val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
                val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()
                Vector3(
                    cameraPanX + cameraDist * cos(pitchRad) * sin(yawRad),
                    cameraDist * sin(pitchRad),
                    cameraPanZ + cameraDist * cos(pitchRad) * cos(yawRad)
                )
            } else {
                Vector3(walkX, 1.65f, walkZ) // Human eye level 1.65m
            }

            val targetPos = if (!isWalkthroughMode) {
                Vector3(cameraPanX, 0f, cameraPanZ)
            } else {
                val wRad = Math.toRadians(walkYawDeg.toDouble()).toFloat()
                Vector3(walkX + sin(wRad) * 4f, 1.65f, walkZ + cos(wRad) * 4f)
            }

            // Light vector
            val lightDir = if (!isNightMode) {
                Vector3(0.5f, 1.2f, 0.7f).normalize()
            } else {
                Vector3(0.0f, 1.0f, 0.0f).normalize()
            }

            // Build 3D mesh geometry
            val meshPolys = mutableListOf<Poly3D>()

            // 1. Floors
            for (room in rooms) {
                if (room.points.size >= 3) {
                    val floorVertices = room.points.map { Vector3(it.x, 0.02f, it.y) }
                    meshPolys.add(
                        Poly3D(
                            vertices = floorVertices,
                            baseColor = Color(room.floorColor),
                            normal = Vector3(0f, 1f, 0f)
                        )
                    )
                }
            }

            // 2. Walls with Thickness, Dual-face colors, and Portals
            for (wall in walls) {
                val h = min(wall.height, wallDisplayHeight)
                val thick = wall.thickness
                val norm = wall.normal
                val n3 = Vector3(norm.x, 0f, norm.y) * (thick / 2f)

                val vStart = Vector3(wall.start.x, 0f, wall.start.y)
                val vEnd = Vector3(wall.end.x, 0f, wall.end.y)

                val innerColor = if (wall.isAccent) Color(wall.accentColor) else Color(wall.innerColor)
                val outerColor = Color(wall.outerColor)

                // Face A (Outer)
                val aBottom1 = vStart + n3
                val aBottom2 = vEnd + n3
                val aTop2 = aBottom2 + Vector3(0f, h, 0f)
                val aTop1 = aBottom1 + Vector3(0f, h, 0f)

                meshPolys.add(
                    Poly3D(
                        vertices = listOf(aBottom1, aBottom2, aTop2, aTop1),
                        baseColor = outerColor,
                        normal = Vector3(norm.x, 0f, norm.y)
                    )
                )

                // Face B (Inner)
                val bBottom1 = vStart - n3
                val bBottom2 = vEnd - n3
                val bTop2 = bBottom2 + Vector3(0f, h, 0f)
                val bTop1 = bBottom1 + Vector3(0f, h, 0f)

                meshPolys.add(
                    Poly3D(
                        vertices = listOf(bBottom2, bBottom1, bTop1, bTop2),
                        baseColor = innerColor,
                        normal = Vector3(-norm.x, 0f, -norm.y)
                    )
                )

                // Wall Top Cap
                meshPolys.add(
                    Poly3D(
                        vertices = listOf(aTop1, aTop2, bTop2, bTop1),
                        baseColor = Color(0xFFD6CEBE),
                        normal = Vector3(0f, 1f, 0f)
                    )
                )

                // If wall has a bump-out (نيش 50 سم أو كمر)، add 3D niche sides!
                if (wall.bumpOut != null) {
                    val bo = wall.bumpOut
                    val t1 = bo.offsetMeters / wall.length.coerceAtLeast(0.01f)
                    val t2 = (bo.offsetMeters + bo.widthMeters) / wall.length.coerceAtLeast(0.01f)

                    val pNicheStart = wall.start.lerp(wall.end, t1)
                    val pNicheEnd = wall.start.lerp(wall.end, t2)
                    val nicheDepthOffset = Vector3(-norm.x, 0f, -norm.y) * bo.depthMeters

                    val nStartBottom = Vector3(pNicheStart.x, 0f, pNicheStart.y) - n3
                    val nEndBottom = Vector3(pNicheEnd.x, 0f, pNicheEnd.y) - n3

                    val nRecessedStart = nStartBottom + nicheDepthOffset
                    val nRecessedEnd = nEndBottom + nicheDepthOffset

                    // Side wall 1 of niche
                    meshPolys.add(
                        Poly3D(
                            vertices = listOf(
                                nStartBottom,
                                nRecessedStart,
                                nRecessedStart + Vector3(0f, h, 0f),
                                nStartBottom + Vector3(0f, h, 0f)
                            ),
                            baseColor = Color(0xFFC4BBAF),
                            normal = Vector3(-norm.y, 0f, norm.x)
                        )
                    )

                    // Side wall 2 of niche
                    meshPolys.add(
                        Poly3D(
                            vertices = listOf(
                                nEndBottom,
                                nRecessedEnd,
                                nRecessedEnd + Vector3(0f, h, 0f),
                                nEndBottom + Vector3(0f, h, 0f)
                            ),
                            baseColor = Color(0xFFC4BBAF),
                            normal = Vector3(norm.y, 0f, -norm.x)
                        )
                    )
                }
            }

            // 3. Columns
            for (col in columns) {
                val hw = col.width / 2f
                val hd = col.depth / 2f
                val ch = wallDisplayHeight

                val c0 = Vector3(col.x - hw, 0f, col.y - hd)
                val c1 = Vector3(col.x + hw, 0f, col.y - hd)
                val c2 = Vector3(col.x + hw, 0f, col.y + hd)
                val c3 = Vector3(col.x - hw, 0f, col.y + hd)

                // 4 vertical column faces
                meshPolys.add(Poly3D(listOf(c0, c1, c1 + Vector3(0f, ch, 0f), c0 + Vector3(0f, ch, 0f)), Color(0xFFE2E8F0), Vector3(0f, 0f, -1f)))
                meshPolys.add(Poly3D(listOf(c1, c2, c2 + Vector3(0f, ch, 0f), c1 + Vector3(0f, ch, 0f)), Color(0xFFCBD5E1), Vector3(1f, 0f, 0f)))
                meshPolys.add(Poly3D(listOf(c2, c3, c3 + Vector3(0f, ch, 0f), c2 + Vector3(0f, ch, 0f)), Color(0xFFE2E8F0), Vector3(0f, 0f, 1f)))
                meshPolys.add(Poly3D(listOf(c3, c0, c0 + Vector3(0f, ch, 0f), c3 + Vector3(0f, ch, 0f)), Color(0xFFCBD5E1), Vector3(-1f, 0f, 0f)))
            }

            // 4. Furniture 3D Boxes
            for (item in furniture) {
                val rad = Math.toRadians(item.rotationDeg.toDouble()).toFloat()
                val cosR = cos(rad)
                val sinR = sin(rad)
                val hw = item.width / 2f
                val hd = item.depth / 2f
                val fh = item.height
                val baseY = item.elevation

                fun localToWorld(lx: Float, lz: Float): Vector3 {
                    val wx = item.x + (lx * cosR - lz * sinR)
                    val wz = item.y + (lx * sinR + lz * cosR)
                    return Vector3(wx, baseY, wz)
                }

                val p0 = localToWorld(-hw, -hd)
                val p1 = localToWorld(hw, -hd)
                val p2 = localToWorld(hw, hd)
                val p3 = localToWorld(-hw, hd)

                val col = Color(item.primaryColor)
                val topCol = Color(item.fabricColor)

                // Furniture top
                meshPolys.add(
                    Poly3D(
                        vertices = listOf(
                            p0 + Vector3(0f, fh, 0f),
                            p1 + Vector3(0f, fh, 0f),
                            p2 + Vector3(0f, fh, 0f),
                            p3 + Vector3(0f, fh, 0f)
                        ),
                        baseColor = topCol,
                        normal = Vector3(0f, 1f, 0f)
                    )
                )

                // Furniture sides
                meshPolys.add(Poly3D(listOf(p0, p1, p1 + Vector3(0f, fh, 0f), p0 + Vector3(0f, fh, 0f)), col, Vector3(0f, 0f, -1f)))
                meshPolys.add(Poly3D(listOf(p1, p2, p2 + Vector3(0f, fh, 0f), p1 + Vector3(0f, fh, 0f)), col, Vector3(1f, 0f, 0f)))
                meshPolys.add(Poly3D(listOf(p2, p3, p3 + Vector3(0f, fh, 0f), p2 + Vector3(0f, fh, 0f)), col, Vector3(0f, 0f, 1f)))
                meshPolys.add(Poly3D(listOf(p3, p0, p0 + Vector3(0f, fh, 0f), p3 + Vector3(0f, fh, 0f)), col, Vector3(-1f, 0f, 0f)))
            }

            // Camera View & Projection Matrix Math
            val forward = (targetPos - camPos).normalize()
            val upHint = Vector3(0f, 1f, 0f)
            val right = forward.cross(upHint).normalize()
            val up = right.cross(forward).normalize()

            val fovDist = if (isWalkthroughMode) canvasW * 0.9f else canvasW * 1.3f
            val halfW = canvasW / 2f
            val halfH = canvasH / 2f

            fun project(v: Vector3): Triple<Float, Float, Float>? {
                val rel = v - camPos
                val zCam = rel.dot(forward)
                if (zCam <= 0.1f) return null // behind camera
                val xCam = rel.dot(right)
                val yCam = rel.dot(up)

                val screenX = halfW + (xCam / zCam) * fovDist
                val screenY = halfH - (yCam / zCam) * fovDist
                return Triple(screenX, screenY, zCam)
            }

            // Compute depth for painter's sort
            for (p in meshPolys) {
                var sumDepth = 0f
                for (v in p.vertices) {
                    sumDepth += (v - camPos).dot(forward)
                }
                p.depth = sumDepth / p.vertices.size
            }

            // Sort back to front (Painter's algorithm)
            meshPolys.sortByDescending { it.depth }

            // Render polygons
            for (p in meshPolys) {
                if (p.depth <= 0.1f) continue

                val projPoints = mutableListOf<Offset>()
                var allValid = true

                for (v in p.vertices) {
                    val pt = project(v)
                    if (pt != null) {
                        projPoints.add(Offset(pt.first, pt.second))
                    } else {
                        allValid = false
                        break
                    }
                }

                if (!allValid || projPoints.size < 3) continue

                // Compute shading from light
                val cosTheta = p.normal.dot(lightDir).coerceIn(0f, 1f)
                val ambient = if (isNightMode) 0.35f else 0.55f
                val diffuse = if (isNightMode) 0.35f else 0.45f
                val lightFactor = ambient + diffuse * cosTheta

                val shadedColor = Color(
                    red = (p.baseColor.red * lightFactor).coerceIn(0f, 1f),
                    green = (p.baseColor.green * lightFactor).coerceIn(0f, 1f),
                    blue = (p.baseColor.blue * lightFactor).coerceIn(0f, 1f),
                    alpha = p.baseColor.alpha
                )

                val path = Path().apply {
                    moveTo(projPoints[0].x, projPoints[0].y)
                    for (k in 1 until projPoints.size) {
                        lineTo(projPoints[k].x, projPoints[k].y)
                    }
                    close()
                }

                drawPath(path = path, color = shadedColor)

                // Edge outline
                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = if (isNightMode) 0.3f else 0.18f),
                    style = Stroke(width = 0.8f)
                )
            }
        }

        // Overlay Controls for 3D Camera & Lighting
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode switch: Orbit vs Walkthrough
            FilledTonalIconButton(
                onClick = { isWalkthroughMode = !isWalkthroughMode },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isWalkthroughMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (isWalkthroughMode) Icons.Default.DirectionsWalk else Icons.Default.Public,
                    contentDescription = "تبديل المنظور",
                    tint = if (isWalkthroughMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Day / Night lighting toggle
            FilledTonalIconButton(
                onClick = { isNightMode = !isNightMode }
            ) {
                Icon(
                    imageVector = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "الإضاءة",
                    tint = if (isNightMode) Color(0xFFF59E0B) else Color(0xFF0284C7)
                )
            }

            // Cutaway walls toggle
            FilledTonalIconButton(
                onClick = { isCutawayMode = !isCutawayMode }
            ) {
                Icon(
                    imageVector = if (isCutawayMode) Icons.Default.Crop else Icons.Default.Home,
                    contentDescription = "قص الحوائط",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Reset camera
            FilledTonalIconButton(
                onClick = {
                    yawDeg = 45f
                    pitchDeg = 35f
                    cameraDist = 16f
                    cameraPanX = 5.5f
                    cameraPanZ = 4.5f
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط الكاميرا")
            }
        }

        // Walkthrough joystick / walk navigation buttons (if in walkthrough mode)
        if (isWalkthroughMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚶 خطوة:", style = MaterialTheme.typography.bodySmall)

                    IconButton(
                        onClick = {
                            val rad = Math.toRadians(walkYawDeg.toDouble()).toFloat()
                            walkX += sin(rad) * 0.4f
                            walkZ += cos(rad) * 0.4f
                        }
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "أمام")
                    }

                    IconButton(
                        onClick = {
                            val rad = Math.toRadians(walkYawDeg.toDouble()).toFloat()
                            walkX -= sin(rad) * 0.4f
                            walkZ -= cos(rad) * 0.4f
                        }
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "خلف")
                    }

                    IconButton(
                        onClick = { walkYawDeg = (walkYawDeg - 25f) % 360f }
                    ) {
                        Icon(Icons.Default.RotateLeft, contentDescription = "لف يسار")
                    }

                    IconButton(
                        onClick = { walkYawDeg = (walkYawDeg + 25f) % 360f }
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "لف يمين")
                    }
                }
            }
        }

        // View Mode Indicator Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Text(
                text = if (isWalkthroughMode) "🚶 زاوية المشي داخل الشقة (عين الإنسان)" else "🧊 منظور 3D عام (دوران 360°)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
