package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalInspectorSheet(
    portal: PortalItem,
    onClose: () -> Unit,
    onUpdatePortal: (PortalItem) -> Unit,
    onDeletePortal: () -> Unit
) {
    var widthInput by remember(portal.width) { mutableStateOf(String.format(Locale.US, "%.2f", portal.width)) }
    var heightInput by remember(portal.height) { mutableStateOf(String.format(Locale.US, "%.2f", portal.height)) }
    var offsetInput by remember(portal.offsetMeters) { mutableStateOf(String.format(Locale.US, "%.2f", portal.offsetMeters)) }
    var elevationInput by remember(portal.elevation) { mutableStateOf(String.format(Locale.US, "%.2f", portal.elevation)) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (portal.type.isWindow) "🪟 مواصفات الشباك" else "🚪 مواصفات الباب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("فتحة فعلية في الجدار", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 1. Type
            Text("النوع:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PortalType.values().forEach { t ->
                    FilterChip(
                        selected = (portal.type == t),
                        onClick = { onUpdatePortal(portal.copy(type = t)) },
                        label = { Text(t.titleAr, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. Dimensions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = {
                        widthInput = it
                        it.toFloatOrNull()?.let { w -> onUpdatePortal(portal.copy(width = w.coerceAtLeast(0.4f))) }
                    },
                    label = { Text("العرض (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = {
                        heightInput = it
                        it.toFloatOrNull()?.let { h -> onUpdatePortal(portal.copy(height = h.coerceAtLeast(0.5f))) }
                    },
                    label = { Text("الارتفاع (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = offsetInput,
                    onValueChange = {
                        offsetInput = it
                        it.toFloatOrNull()?.let { o -> onUpdatePortal(portal.copy(offsetMeters = o.coerceAtLeast(0f))) }
                    },
                    label = { Text("الموقع بالحيط (م)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            if (portal.type.isWindow) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = elevationInput,
                    onValueChange = {
                        elevationInput = it
                        it.toFloatOrNull()?.let { el -> onUpdatePortal(portal.copy(elevation = el.coerceAtLeast(0f))) }
                    },
                    label = { Text("ارتفاع الجلسة عن الأرض (م)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                // Door swing
                Spacer(Modifier.height(8.dp))
                Text("اتجاه فتح الباب والقوس بالمخطط:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DoorSwing.values().forEach { swing ->
                        FilterChip(
                            selected = (portal.swing == swing),
                            onClick = { onUpdatePortal(portal.copy(swing = swing)) },
                            label = { Text(swing.titleAr, fontSize = 10.sp) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 3. Glass Type
            Text("نوع الزجاج:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassType.values().forEach { gt ->
                    FilterChip(
                        selected = (portal.glassType == gt),
                        onClick = { onUpdatePortal(portal.copy(glassType = gt)) },
                        label = { Text(gt.titleAr, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Delete
            Button(
                onClick = onDeletePortal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("حذف هذه الفتحة")
            }
        }
    }
}
