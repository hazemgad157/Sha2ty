package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FurnitureItem
import com.example.model.PurchaseStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureInspectorSheet(
    item: FurnitureItem,
    onClose: () -> Unit,
    onUpdateItem: (FurnitureItem) -> Unit,
    onDuplicateItem: () -> Unit,
    onDeleteItem: () -> Unit
) {
    var widthInput by remember(item.width) { mutableStateOf(String.format(Locale.US, "%.2f", item.width)) }
    var depthInput by remember(item.depth) { mutableStateOf(String.format(Locale.US, "%.2f", item.depth)) }
    var heightInput by remember(item.height) { mutableStateOf(String.format(Locale.US, "%.2f", item.height)) }
    var priceInput by remember(item.priceEgp) { mutableStateOf(item.priceEgp.toInt().toString()) }

    val popularColors = listOf(
        0xFF1D3557 to "كحلي",
        0xFF37474F to "رمادي فحمي",
        0xFF5D4037 to "بني زان",
        0xFF8D6E63 to "خشب بيج",
        0xFFB45309 to "هافان دافئ",
        0xFFD97706 to "عسلي تيك",
        0xFF2E7D32 to "زيتي ملكي",
        0xFFE2DDD5 to "رخام أبيض",
        0xFFF1ECE1 to "كتان أوف وايت"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("خامة: ${item.materialName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }

                Row {
                    IconButton(onClick = { onUpdateItem(item.copy(isLocked = !item.isLocked)) }) {
                        Icon(
                            imageVector = if (item.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "قفل القطعة",
                            tint = if (item.isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 1. Dimensions (Width, Depth, Height)
            Text("📏 الأبعاد الواقعية (بالمتر)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = {
                        widthInput = it
                        it.toFloatOrNull()?.let { w -> onUpdateItem(item.copy(width = w.coerceAtLeast(0.1f))) }
                    },
                    label = { Text("العرض (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = depthInput,
                    onValueChange = {
                        depthInput = it
                        it.toFloatOrNull()?.let { d -> onUpdateItem(item.copy(depth = d.coerceAtLeast(0.1f))) }
                    },
                    label = { Text("العمق (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = {
                        heightInput = it
                        it.toFloatOrNull()?.let { h -> onUpdateItem(item.copy(height = h.coerceAtLeast(0.1f))) }
                    },
                    label = { Text("الارتفاع (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))

            // 2. Rotation & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onUpdateItem(item.copy(rotationDeg = (item.rotationDeg + 90f) % 360f)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تدوير 90° (${item.rotationDeg.toInt()}°)")
                }

                OutlinedButton(
                    onClick = onDuplicateItem,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("نسخة طبق الأصل")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 3. Multi-part Coloring (هيكل أساسي، قماش/تنجيد، أرجل)
            Text("🎨 ألوان الأجزاء والخامات", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Text("لون الهيكل الأساسي / الخشب:", fontSize = 12.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(popularColors) { (col, name) ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(col))
                            .border(
                                width = if (item.primaryColor == col) 2.5.dp else 1.dp,
                                color = if (item.primaryColor == col) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { onUpdateItem(item.copy(primaryColor = col)) }
                    )
                }
            }

            Text("لون القماش / التنجيد:", fontSize = 12.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(popularColors) { (col, name) ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(col))
                            .border(
                                width = if (item.fabricColor == col) 2.5.dp else 1.dp,
                                color = if (item.fabricColor == col) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { onUpdateItem(item.copy(fabricColor = col)) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 4. Budget & Purchase Status (فكرة / محجوزة / تم الشراء)
            Text("💰 التكلفة والميزانية", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        it.toDoubleOrNull()?.let { p -> onUpdateItem(item.copy(priceEgp = p)) }
                    },
                    label = { Text("السعر التقديري (ج.م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Column(modifier = Modifier.weight(1.5f)) {
                    Text("حالة الشراء:", fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PurchaseStatus.values().forEach { status ->
                            FilterChip(
                                selected = (item.purchaseStatus == status),
                                onClick = { onUpdateItem(item.copy(purchaseStatus = status)) },
                                label = { Text(status.titleAr, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Delete
            Button(
                onClick = onDeleteItem,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("حذف هذه القطعة")
            }
        }
    }
}
