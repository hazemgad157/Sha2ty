package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogBottomSheet(
    onDismiss: () -> Unit,
    onAddFurniture: (FurnitureItem) -> Unit,
    onAddPortal: (PortalType) -> Unit
) {
    var selectedCat by remember { mutableStateOf<FurnitureCategory?>(FurnitureCategory.LIVING) }
    var isPortalsTab by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛋️ كتالوج العفش والأبواب والديكور",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن قطعة (سرير، ركنة، بانيو، باب...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Categories horizontal row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = isPortalsTab,
                        onClick = {
                            isPortalsTab = true
                            selectedCat = null
                        },
                        label = { Text("🚪 أبواب وشبابيك") }
                    )
                }

                items(FurnitureCategory.values()) { cat ->
                    FilterChip(
                        selected = (!isPortalsTab && selectedCat == cat),
                        onClick = {
                            isPortalsTab = false
                            selectedCat = cat
                        },
                        label = { Text("${cat.icon} ${cat.titleAr}") }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Items list
            if (isPortalsTab) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(PortalType.values()) { pt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddPortal(pt)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (pt.isWindow) "🪟" else "🚪", fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(pt.titleAr, fontWeight = FontWeight.Bold)
                                        Text(
                                            if (pt.isWindow) "شباك حقيقي بفتحة في الجدار" else "باب حقيقي مع قوس اتجاه الفتح",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        onAddPortal(pt)
                                        onDismiss()
                                    }
                                ) {
                                    Text("إضافة")
                                }
                            }
                        }
                    }
                }
            } else {
                val filteredItems = CatalogData.FURNITURE_CATALOG.filter { item ->
                    (selectedCat == null || item.category == selectedCat) &&
                    (searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(filteredItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newItem = item.copy(
                                        id = "furn_" + UUID.randomUUID().toString().take(6),
                                        x = 3.5f,
                                        y = 3.5f
                                    )
                                    onAddFurniture(newItem)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(item.primaryColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.category.icon, fontSize = 20.sp)
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(
                                            "المقاس: ${item.width} × ${item.depth} × ${item.height} م",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "السعر التقديري: ${item.priceEgp.toInt()} ج.م",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val newItem = item.copy(
                                            id = "furn_" + UUID.randomUUID().toString().take(6),
                                            x = 3.5f,
                                            y = 3.5f
                                        )
                                        onAddFurniture(newItem)
                                        onDismiss()
                                    }
                                ) {
                                    Text("إضافة")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
