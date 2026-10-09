package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.QuantitiesEstimate
import com.example.model.FurnitureItem
import com.example.model.PurchaseStatus

@Composable
fun BudgetDialog(
    estimate: QuantitiesEstimate,
    furniture: List<FurnitureItem>,
    onDismiss: () -> Unit,
    onExportTable: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊 المقايسة وحساب الكميات والميزانية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary KPI Cards
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("إجمالي تكلفة العفش والديكور", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "${estimate.furnitureTotalEgp.toInt()} ج.م",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تم الشراء: ${estimate.furniturePurchasedEgp.toInt()} ج.م", fontSize = 11.sp, color = Color(0xFF10B981))
                                Text("محجوز: ${estimate.furnitureReservedEgp.toInt()} ج.م", fontSize = 11.sp, color = Color(0xFFD97706))
                                Text("مقترح: ${estimate.furnitureIdeasEgp.toInt()} ج.م", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                // Materials & BOQ
                item {
                    Text("🧱 كميات التشطيبات (دهان وأرضيات)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صافي مساحة الحوائط:", fontSize = 12.sp)
                                Text("${String.format("%.1f", estimate.totalWallNetAreaM2)} م²", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("كمية الدهان المقدرة (وجهين):", fontSize = 12.sp)
                                Text("${String.format("%.1f", estimate.paintLitersNeeded)} لتر (${estimate.paintCans15L} بستلة 15L)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي مسطح الأرضيات:", fontSize = 12.sp)
                                Text("${String.format("%.1f", estimate.totalFloorAreaM2)} م²", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صناديق السيراميك والبورسلين (مع الهالك):", fontSize = 12.sp)
                                Text("${estimate.ceramicBoxesNeeded} كرتونة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("أطوال الكرانيش والوزرات:", fontSize = 12.sp)
                                Text("${String.format("%.1f", estimate.cornicesLinearMeters)} متر طولي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Rooms Breakdown
                item {
                    Text("🏠 تفصيل الغرف والمساحات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(estimate.roomEstimates) { room ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(room.roomName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("خامة: ${room.floorMaterial}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${String.format("%.1f", room.areaM2)} م²", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${room.tileBoxes} كرتونة", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Furniture List with statuses
                item {
                    Text("🛋️ قايمة العفش وحالة الشراء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(furniture) { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(f.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = f.purchaseStatus.titleAr,
                                fontSize = 10.sp,
                                color = Color(f.purchaseStatus.colorHex),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("${f.priceEgp.toInt()} ج.م", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onExportTable) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("نسخ جدول المقايسة للمحلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
