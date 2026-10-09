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
import com.example.model.FloorMaterial
import com.example.model.RoomZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInspectorSheet(
    room: RoomZone,
    onClose: () -> Unit,
    onUpdateRoom: (RoomZone) -> Unit
) {
    var nameInput by remember(room.name) { mutableStateOf(room.name) }
    var ceilingInput by remember(room.ceilingHeight) { mutableStateOf(room.ceilingHeight.toString()) }

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
                Text(
                    text = "🏠 مواصفات الغرفة والأرضيات والأسقف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    onUpdateRoom(room.copy(name = it))
                },
                label = { Text("اسم الغرفة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // Flooring materials
            Text("خامة الأرضية ومقاس البلاطة:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FloorMaterial.values().forEach { mat ->
                    FilterChip(
                        selected = (room.floorMaterial == mat),
                        onClick = {
                            onUpdateRoom(
                                room.copy(
                                    floorMaterial = mat,
                                    floorColor = mat.defaultColor,
                                    tileSizeCm = mat.defaultTileSizeCm
                                )
                            )
                        },
                        label = { Text(mat.titleAr, fontSize = 11.sp) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))

            // Ceilings & Lighting
            Text("السقف والجبس بورد والإنارة:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = ceilingInput,
                onValueChange = {
                    ceilingInput = it
                    it.toFloatOrNull()?.let { ch -> onUpdateRoom(room.copy(ceilingHeight = ch)) }
                },
                label = { Text("ارتفاع السقف (م) مثلاً 2.80") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("بيت نور جبس بورد (Cove Lighting):")
                Switch(
                    checked = room.hasGypsumCove,
                    onCheckedChange = { onUpdateRoom(room.copy(hasGypsumCove = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("كرانيش فيوتك / جبس بلدي:")
                Switch(
                    checked = room.hasCornice,
                    onCheckedChange = { onUpdateRoom(room.copy(hasCornice = it)) }
                )
            }
        }
    }
}
