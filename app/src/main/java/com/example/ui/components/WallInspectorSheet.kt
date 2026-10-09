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
import com.example.data.CatalogData
import com.example.model.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallInspectorSheet(
    wall: WallSegment,
    onClose: () -> Unit,
    onUpdateWall: (WallSegment) -> Unit,
    onResizeWall: (newLength: Float) -> Unit,
    onSplitWall: (distance: Float) -> Unit,
    onDeleteWall: () -> Unit,
    onShowSectionView: () -> Unit
) {
    var lengthInput by remember(wall.length) { mutableStateOf(String.format(Locale.US, "%.2f", wall.length)) }
    var splitDistInput by remember { mutableStateOf("1.20") }
    var nicheWidthInput by remember { mutableStateOf("1.20") }
    var nicheDepthInput by remember { mutableStateOf("0.50") }

    var showNicheDialog by remember { mutableStateOf(false) }
    var showCutoutDialog by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Architecture, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "تحكم كامل في الحائط",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row {
                    FilledTonalButton(onClick = onShowSectionView) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("اعرض مقطع", fontSize = 12.sp)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 1. Precise Dimensions: Length, Thickness, Height
            Text("📐 المقاسات الدقيقة", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = lengthInput,
                    onValueChange = {
                        lengthInput = it
                        it.toFloatOrNull()?.let { len -> onResizeWall(len) }
                    },
                    label = { Text("الطول (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Thickness Quick Buttons (10cm, 12cm, 15cm, 20cm, 25cm)
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("السُمك:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.10f to "10سم", 0.12f to "12سم", 0.15f to "15سم", 0.20f to "20سم", 0.25f to "25سم حائط حامل").forEach { (thick, label) ->
                            FilterChip(
                                selected = (wall.thickness == thick),
                                onClick = { onUpdateWall(wall.copy(thickness = thick)) },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Height: Full (2.8m) vs Half-height (0.90m / 1.20m)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الارتفاع:", fontSize = 12.sp)
                FilterChip(
                    selected = (wall.height >= 2.5f),
                    onClick = { onUpdateWall(wall.copy(height = 2.80f)) },
                    label = { Text("كامل 2.80م") }
                )
                FilterChip(
                    selected = (wall.height == 1.20f),
                    onClick = { onUpdateWall(wall.copy(height = 1.20f)) },
                    label = { Text("بار مطبخ 1.20م") }
                )
                FilterChip(
                    selected = (wall.height == 0.90f),
                    onClick = { onUpdateWall(wall.copy(height = 0.90f)) },
                    label = { Text("حاجز 90سم") }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 2. Wall Splitting (تقسيم الحائط لأجزاء عند مسافة معينة)
            Text("✂️ تقسيم الحائط لأجزاء", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = splitDistInput,
                    onValueChange = { splitDistInput = it },
                    label = { Text("مسافة من الركن (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        splitDistInput.toFloatOrNull()?.let { onSplitWall(it) }
                    }
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("قسّم الحائط")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 3. Wall Features: Niche Bump-out (تدخيل 50 سم) & Cutout Opening (شيل جزء)
            Text("🧱 نيش / إزاحة وتجاويف وفتحات", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (wall.bumpOut == null) {
                    OutlinedButton(onClick = { showNicheDialog = true }) {
                        Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إزاحة / نيش 50 سم")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onUpdateWall(wall.copy(bumpOut = null)) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إلغاء النيش (${wall.bumpOut.depthMeters}م)")
                    }
                }

                OutlinedButton(onClick = { showCutoutDialog = true }) {
                    Icon(Icons.Default.CropFree, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("شيل جزء / فتحة ريسبشن")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 4. Dual-Face Colors & Finishes (وجه داخلي وجه خارجي وحائط مميز وبوازيري)
            Text("🎨 ألوان وتشطيبات الوجهين", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accent Wall Toggle
                FilterChip(
                    selected = wall.isAccent,
                    onClick = { onUpdateWall(wall.copy(isAccent = !wall.isAccent)) },
                    label = { Text("⭐ حائط مميز (Accent)") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )

                // Dado Toggle (بوازيري نصين)
                FilterChip(
                    selected = wall.isDado,
                    onClick = { onUpdateWall(wall.copy(isDado = !wall.isDado)) },
                    label = { Text("بوازيري (نصفين)") }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Real Paints Swatches (Jotun / Scib)
            Text("اختر درجة الدهان (كود جوتن معتمد):", fontSize = 12.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(CatalogData.REAL_PAINTS) { paint ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onUpdateWall(
                                    wall.copy(
                                        innerColor = paint.colorHex,
                                        paintBrandCode = "${paint.brand} ${paint.code} ${paint.nameAr}"
                                    )
                                )
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(paint.colorHex))
                                .border(
                                    width = if (wall.innerColor == paint.colorHex) 2.5.dp else 1.dp,
                                    color = if (wall.innerColor == paint.colorHex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                        )
                        Text(paint.nameAr.take(8), fontSize = 10.sp, maxLines = 1)
                        Text(paint.code, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("الكود المعتمد: ${wall.paintBrandCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)

            Spacer(Modifier.height(8.dp))

            // Delete Wall Button
            Button(
                onClick = onDeleteWall,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("حذف هذا الحائط")
            }
        }
    }

    // Dialog for Niche Bump-out (تدخيل 50 سم)
    if (showNicheDialog) {
        AlertDialog(
            onDismissRequest = { showNicheDialog = false },
            title = { Text("إزاحة الحائط / نيش ديكور") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تدخيل جزء من الحائط للداخل أو الخارج مع تقفيل الجوانب الثلاثة تلقائياً.")
                    OutlinedTextField(
                        value = nicheWidthInput,
                        onValueChange = { nicheWidthInput = it },
                        label = { Text("عرض النيش (م) مثلاً 1.20") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nicheDepthInput,
                        onValueChange = { nicheDepthInput = it },
                        label = { Text("عمق التدخيل (م) مثلاً 0.50") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = nicheWidthInput.toFloatOrNull() ?: 1.20f
                        val d = nicheDepthInput.toFloatOrNull() ?: 0.50f
                        val newBump = WallBumpOut(
                            id = "bump_user",
                            offsetMeters = 0.50f,
                            widthMeters = w,
                            depthMeters = d,
                            isOutward = false
                        )
                        onUpdateWall(wall.copy(bumpOut = newBump))
                        showNicheDialog = false
                    }
                ) {
                    Text("تطبيق الإزاحة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicheDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog for Opening / Cutout (شيل جزء من الحائط)
    if (showCutoutDialog) {
        var opWidth by remember { mutableStateOf("1.40") }
        var isArched by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCutoutDialog = false },
            title = { Text("شيل جزء من الحائط / مدخل مفتوح") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("عمل فتحة بدون باب (مدخل ريسبشن مفتوح أو عقد إسلامي/روماني).")
                    OutlinedTextField(
                        value = opWidth,
                        onValueChange = { opWidth = it },
                        label = { Text("عرض الفتحة (م)") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isArched, onCheckedChange = { isArched = it })
                        Spacer(Modifier.width(4.dp))
                        Text("فتحة بقوس علوي (Arched)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = opWidth.toFloatOrNull() ?: 1.40f
                        val newOp = WallOpening(
                            id = "op_user",
                            offsetMeters = 0.40f,
                            widthMeters = w,
                            heightMeters = 2.40f,
                            isArched = isArched
                        )
                        onUpdateWall(wall.copy(openings = wall.openings + newOp))
                        showCutoutDialog = false
                    }
                ) {
                    Text("إضافة الفتحة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCutoutDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
