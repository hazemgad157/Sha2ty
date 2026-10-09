package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WallSegment

@Composable
fun WallSectionDialog(
    wall: WallSegment,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("مقطع هندسي رأسي للحائط (Cross-Section)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سُمك الحائط: ${(wall.thickness * 100).toInt()} سم | الارتفاع: ${wall.height} م",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (wall.bumpOut != null) {
                    Text(
                        text = "يوجد نيش / تجويف بعمق ${(wall.bumpOut.depthMeters * 100).toInt()} سم وعرض ${wall.bumpOut.widthMeters} م",
                        fontSize = 12.sp,
                        color = Color(0xFFD97706)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Canvas illustrating wall section
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 240.dp)
                        .background(Color(0xFFF8F7F4), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height

                        val wallWidth = w * 0.40f
                        val wallX = (w - wallWidth) / 2f
                        val floorY = h - 20f
                        val ceilingY = 20f
                        val wallH = floorY - ceilingY

                        // 1. Concrete Slab Bottom & Top
                        drawRect(
                            color = Color(0xFF94A3B8),
                            topLeft = Offset(0f, floorY),
                            size = Size(w, 20f)
                        )
                        drawRect(
                            color = Color(0xFF94A3B8),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, ceilingY)
                        )

                        // 2. Wall Core (Brick / Block)
                        drawRect(
                            color = Color(0xFFD7CCC8),
                            topLeft = Offset(wallX, ceilingY),
                            size = Size(wallWidth, wallH)
                        )

                        // 3. Wall Finishes (Outer face left, Inner face right)
                        drawLine(
                            color = Color(wall.outerColor),
                            start = Offset(wallX, ceilingY),
                            end = Offset(wallX, floorY),
                            strokeWidth = 6f
                        )

                        val innerColor = if (wall.isAccent) Color(wall.accentColor) else Color(wall.innerColor)
                        drawLine(
                            color = innerColor,
                            start = Offset(wallX + wallWidth, ceilingY),
                            end = Offset(wallX + wallWidth, floorY),
                            strokeWidth = 6f
                        )

                        // 4. If Dado (بوازيري): split line
                        if (wall.isDado) {
                            val dadoY = floorY - (wallH * 0.35f)
                            drawLine(
                                color = Color(wall.dadoBottomColor),
                                start = Offset(wallX + wallWidth, dadoY),
                                end = Offset(wallX + wallWidth, floorY),
                                strokeWidth = 8f
                            )
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(wallX + wallWidth - 4f, dadoY),
                                end = Offset(wallX + wallWidth + 8f, dadoY),
                                strokeWidth = 3f
                            )
                        }

                        // 5. If Bump-Out / Niche: draw recess cutout
                        if (wall.bumpOut != null) {
                            val nicheH = wallH * 0.45f
                            val nicheY = ceilingY + (wallH * 0.25f)
                            val nicheDepthPx = wallWidth * 0.65f

                            val nichePath = Path().apply {
                                moveTo(wallX + wallWidth, nicheY)
                                lineTo(wallX + wallWidth - nicheDepthPx, nicheY)
                                lineTo(wallX + wallWidth - nicheDepthPx, nicheY + nicheH)
                                lineTo(wallX + wallWidth, nicheY + nicheH)
                            }
                            drawPath(
                                path = nichePath,
                                color = Color(0xFFFFFBEB)
                            )
                            drawPath(
                                path = nichePath,
                                color = Color(0xFFD97706),
                                style = Stroke(width = 3f)
                            )
                        }

                        // 6. Skirting (الوزرة) & Cornice (الكورنيشة)
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(wallX + wallWidth, floorY - 14f),
                            size = Size(8f, 14f)
                        )
                        drawRect(
                            color = Color(0xFFECEFF1),
                            topLeft = Offset(wallX + wallWidth, ceilingY),
                            size = Size(10f, 12f)
                        )

                        // Wall border
                        drawRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(wallX, ceilingY),
                            size = Size(wallWidth, wallH),
                            style = Stroke(width = 2f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("دهان معتمد: ${wall.paintBrandCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("تم") }
        }
    )
}
