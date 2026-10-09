package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entities.ProjectEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectsManagerDialog(
    savedProjects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSaveCurrent: (projectName: String, clientName: String, notes: String) -> Unit,
    onLoadProject: (ProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    var showSaveForm by remember { mutableStateOf(false) }
    var projectNameInput by remember { mutableStateOf("") }
    var clientNameInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (showSaveForm) "💾 حفظ المشروع الحالي" else "📂 مشاريعي المحفوظة (Room DB)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            if (showSaveForm) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "احفظ تصميم شقتك الحالي في قاعدة البيانات المحلية للرجوع إليه وتعديله في أي وقت.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = projectNameInput,
                        onValueChange = { projectNameInput = it },
                        label = { Text("اسم المشروع (مثلاً: شقتي التجمع الخامس)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientNameInput,
                        onValueChange = { clientNameInput = it },
                        label = { Text("أسماء أصحاب الشقة (مثلاً: حازم ونور)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات / النمط المفضل") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showSaveForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("حفظ التصميم الحالي كمشروع جديد")
                    }

                    HorizontalDivider(Modifier.padding(vertical = 4.dp))

                    if (savedProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("لا توجد مشاريع محفوظة بعد.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("اضغط على الزر أعلاه لحفظ تصميمك الحالي في قاعدة بيانات الهاتف.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 380.dp)
                        ) {
                            items(savedProjects) { project ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onLoadProject(project)
                                            onDismiss()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(project.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (project.clientName.isNotBlank()) {
                                                Text("أصحاب الشقة: ${project.clientName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                "المساحة: ${String.format("%.1f", project.totalFloorAreaM2)} م² | التكلفة: ${project.totalEstimatedBudgetEgp.toInt()} ج.م",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "تاريخ الحفظ: ${dateFormat.format(Date(project.updatedAt))}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    onLoadProject(project)
                                                    onDismiss()
                                                }
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = "فتح المشروع", tint = MaterialTheme.colorScheme.primary)
                                            }

                                            IconButton(
                                                onClick = { onDeleteProject(project.projectId) }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف المشروع", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showSaveForm) {
                Button(
                    onClick = {
                        val name = if (projectNameInput.isBlank()) "تصميم شقتي ${savedProjects.size + 1}" else projectNameInput
                        onSaveCurrent(name, clientNameInput, notesInput)
                        showSaveForm = false
                    }
                ) {
                    Text("تأكيد الحفظ")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("إغلاق") }
            }
        },
        dismissButton = {
            if (showSaveForm) {
                TextButton(onClick = { showSaveForm = false }) { Text("رجوع") }
            }
        }
    )
}
