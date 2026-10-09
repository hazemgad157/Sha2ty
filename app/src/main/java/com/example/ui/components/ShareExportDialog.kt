package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShareExportDialog(
    planCode: String,
    reportText: String,
    onDismiss: () -> Unit,
    onCopyCode: () -> Unit,
    onCopyReport: () -> Unit,
    onLoadCode: (String) -> Unit
) {
    var inputCode by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = كود المشاركة, 1 = تقرير المقاول

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("💑 مشاركة وتصدير التصميم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("كود المشاركة") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("تقرير المقاول/النجار") }
                    )
                }

                if (selectedTab == 0) {
                    Text(
                        text = "ابعت الكود لحبيبتك، وهي تلزقه هنا وتدوس (فتح التصميم) عشان تختاروا وتعدّلوا سوا.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = planCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("كود تصميمك الحالي") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = onCopyCode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("نسخ كود التصميم للحافظة")
                    }

                    HorizontalDivider(Modifier.padding(vertical = 4.dp))

                    Text("فتح كود مستلم:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it },
                        placeholder = { Text("الصق كود التصميم المستلم هنا...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedButton(
                        onClick = {
                            if (inputCode.isNotBlank()) {
                                onLoadCode(inputCode)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("فتح التصميم المستلم")
                    }
                } else {
                    Text(
                        text = "تقرير مفصل بجميع مقاسات الغرف ومواصفات الحوائط والعفش يتبعت واتساب للنجار أو المقاول.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reportText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("التقرير الهندسي للمقاول") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        maxLines = 8
                    )

                    Button(
                        onClick = onCopyReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("نسخ التقرير لواتساب المقاول")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
