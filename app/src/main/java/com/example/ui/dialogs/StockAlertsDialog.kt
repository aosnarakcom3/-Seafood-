package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.StockStatusChip
import com.example.ui.components.numberFormatter
import com.example.ui.theme.StockCriticalColor

@Composable
fun StockAlertsDialog(
    viewModel: InventoryViewModel,
    onRestockMaterial: (RawMaterial) -> Unit,
    onDismiss: () -> Unit
) {
    val lowMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val lowProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 600.dp)
                .testTag("stock_alerts_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ศูนย์แจ้งเตือนสต็อก & สั่งซื้อ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "รายการที่ต่ำกว่าเกณฑ์ขั้นต่ำ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (lowMaterials.isEmpty() && lowProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "สต็อกสินค้าและวัตถุดิบทุกรายการอยู่ในเกณฑ์ปกติ!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Raw Materials
                        if (lowMaterials.isNotEmpty()) {
                            item {
                                Text(
                                    text = "วัตถุดิบใกล้หมด (${lowMaterials.size} รายการ):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            items(lowMaterials) { mat ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = "คงเหลือ: ${numberFormatter.format(mat.currentStock)} ${mat.unit} (เกณฑ์: ${numberFormatter.format(mat.minStockThreshold)})",
                                                fontSize = 11.sp,
                                                color = StockCriticalColor,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onRestockMaterial(mat)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ สั่งซื้อ/รับเข้า", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Products
                        if (lowProducts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "สินค้าสำเร็จรูปใกล้หมด (${lowProducts.size} รายการ):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            items(lowProducts) { prod ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = "คงเหลือ: ${prod.currentStock} ชิ้น (เกณฑ์: ${prod.minStockThreshold} ชิ้น)",
                                                fontSize = 11.sp,
                                                color = StockCriticalColor,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
