package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.StockStatusChip
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockLowColor

@Composable
fun StockAlertsScreen(
    viewModel: InventoryViewModel,
    onRestockMaterial: (RawMaterial) -> Unit,
    onEditMaterial: (RawMaterial) -> Unit,
    onProduceProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val lowProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    var filterType by remember { mutableStateOf("ALL") } // ALL, MATERIALS, PRODUCTS

    val displayedMaterials = if (filterType == "PRODUCTS") emptyList() else lowMaterials
    val displayedProducts = if (filterType == "MATERIALS") emptyList() else lowProducts

    val totalCount = lowMaterials.size + lowProducts.size

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("stock_alerts_screen")
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (totalCount > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (totalCount > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (totalCount > 0) Icons.Default.NotificationsActive else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "แจ้งเตือนสต็อกเหลือน้อย",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalCount > 0) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalCount > 0) "พบรายการต่ำกว่าเกณฑ์ขั้นต่ำ $totalCount รายการ กรุณาสั่งซื้อหรือเติมสต็อก"
                            else "สต็อกวัตถุดิบและสินค้าทุกรายการอยู่ในเกณฑ์ปกติ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (totalCount > 0) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("ทั้งหมด ($totalCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                FilterChip(
                    selected = filterType == "MATERIALS",
                    onClick = { filterType = "MATERIALS" },
                    label = { Text("วัตถุดิบ (${lowMaterials.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                FilterChip(
                    selected = filterType == "PRODUCTS",
                    onClick = { filterType = "PRODUCTS" },
                    label = { Text("สินค้าสำเร็จรูป (${lowProducts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }

        // Empty state
        if (totalCount == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ไม่มีรายการที่สต็อกเหลือน้อย",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ระบบจะแจ้งเตือนอัตโนมัติเมื่อมีรายการที่สต็อกต่ำกว่าเกณฑ์ขั้นต่ำ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section: Raw Materials
        if (displayedMaterials.isNotEmpty()) {
            item {
                Text(
                    text = "📦 วัตถุดิบที่ต้องสั่งซื้อด่วน (${displayedMaterials.size} รายการ):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            items(displayedMaterials) { mat ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = mat.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (mat.sku.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(mat.sku, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            StockStatusChip(
                                currentStock = mat.currentStock,
                                minThreshold = mat.minStockThreshold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stock gauge
                        val ratio = if (mat.minStockThreshold > 0) {
                            (mat.currentStock / mat.minStockThreshold).coerceIn(0.0, 1.0).toFloat()
                        } else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "คงเหลือ: ${numberFormatter.format(mat.currentStock)} ${mat.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (mat.currentStock <= 0) StockCriticalColor else StockLowColor
                            )
                            Text(
                                text = "เกณฑ์ขั้นต่ำ: ${numberFormatter.format(mat.minStockThreshold)} ${mat.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { ratio },
                            color = if (mat.currentStock <= 0) StockCriticalColor else StockLowColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onRestockMaterial(mat) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ รับเข้าสต็อกทันที", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onEditMaterial(mat) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("แก้ไขเกณฑ์", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Finished Products
        if (displayedProducts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏷️ สินค้าสำเร็จรูปใกล้หมด (${displayedProducts.size} รายการ):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            items(displayedProducts) { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = prod.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (prod.currentStock <= 0) "หมดสต็อก" else "สต็อกเหลือน้อย",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "คงเหลือ: ${prod.currentStock} ชิ้น",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "เกณฑ์เตือน: ${prod.minStockThreshold} ชิ้น",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onProduceProduct(prod) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ตัดวัตถุดิบเพื่อผลิตเพิ่ม", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
