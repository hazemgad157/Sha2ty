package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComparisonDialog(
    currentVariant: String, // "A" or "B"
    onSelectVariant: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("⚖️ مقارنة نسختين من التصميم (A و B)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "تقدروا تعملوا نسختين للتصميم (مثلاً تصميم A بألوان بيج وأرضية باركيه، وتصميم B بألوان كحلي ومودرن) وتتنقلوا بينهم للمقارنة والتصويت على الأفضل!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentVariant == "A") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("النسخة أ (Design A)", fontWeight = FontWeight.Bold)
                            Text("النمط الدافئ / الكلاسيك الهادئ", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onSelectVariant("A") },
                            enabled = currentVariant != "A"
                        ) {
                            Text(if (currentVariant == "A") "النشط حالياً ✓" else "عرض")
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentVariant == "B") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("النسخة ب (Design B)", fontWeight = FontWeight.Bold)
                            Text("النمط المودرن / الألوان الجريئة", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onSelectVariant("B") },
                            enabled = currentVariant != "B"
                        ) {
                            Text(if (currentVariant == "B") "النشط حالياً ✓" else "عرض")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
