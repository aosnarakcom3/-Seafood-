package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ProductWithRecipeDetails
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.StockStatusChip
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter

@Composable
fun ScannedResultActionDialog(
    scannedCode: String,
    matchedProduct: ProductWithRecipeDetails?,
    matchedMaterial: RawMaterial?,
    viewModel: InventoryViewModel,
    onDeductProduct: (ProductWithRecipeDetails) -> Unit,
    onProduceProduct: (ProductWithRecipeDetails) -> Unit,
    onEditProduct: (ProductWithRecipeDetails) -> Unit,
    onAddProductWithCode: (String) -> Unit,
    onAddMaterialWithCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawMaterialQuickDelta by remember { mutableStateOf("") }
    var quickActionType by remember { mutableStateOf<String?>(null) } // "ADD" or "REMOVE"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("scanned_result_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header: Scanned Code badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "รหัสที่สแกนได้",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = scannedCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CASE 1: MATCHED PRODUCT
                if (matchedProduct != null) {
                    val p = matchedProduct.product
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "สินค้าสำเร็จรูป: ${p.category}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "คงเหลือ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val stockCount = if (p.trackFinishedStock) p.currentStock else matchedProduct.maxProducibleCount
                                    Text(
                                        text = "$stockCount ชิ้น",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (stockCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "สูตรตัดวัตถุดิบ: ${matchedProduct.ingredients.size} รายการ (ทำได้ ${matchedProduct.maxProducibleCount} ชิ้น)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (p.trackFinishedStock) {
                                Text(
                                    text = "สต็อกสำเร็จรูปพร้อมขาย: ${p.currentStock} ชิ้น",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("เลือกการดำเนินการ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 1: Deduct Stock / Sell
                    Button(
                        onClick = {
                            onDismiss()
                            onDeductProduct(matchedProduct)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("scan_action_deduct")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ตัดสต็อก / บันทึกการขาย (Deduct Stock)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 2: Batch Produce
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onProduceProduct(matchedProduct)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ตัดวัตถุดิบเพื่อผลิตเข้าสต็อก (Produce)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 3: Edit details
                    TextButton(
                        onClick = {
                            onDismiss()
                            onEditProduct(matchedProduct)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("ดูรายละเอียดและแก้ไขสูตร BOM")
                    }

                // CASE 2: MATCHED RAW MATERIAL
                } else if (matchedMaterial != null) {
                    val mat = matchedMaterial
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "วัตถุดิบคลัง: ${mat.category}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                StockStatusChip(
                                    currentStock = mat.currentStock,
                                    minThreshold = mat.minStockThreshold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "สต็อกคงเหลือ: ${numberFormatter.format(mat.currentStock)} ${mat.unit}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Add or Quick Remove field
                    if (quickActionType != null) {
                        val isAdd = quickActionType == "ADD"
                        Text(
                            text = if (isAdd) "ระบุจำนวนรับเข้า (${mat.unit}):" else "ระบุจำนวนตัดยอด (${mat.unit}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = rawMaterialQuickDelta,
                                onValueChange = { rawMaterialQuickDelta = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                suffix = { Text(mat.unit) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val delta = rawMaterialQuickDelta.replace(",", "").toDoubleOrNull() ?: 0.0
                                    if (delta > 0) {
                                        if (isAdd) {
                                            viewModel.restockMaterial(mat.id, delta, 0.0, "สแกนบาร์โค้ดรับเข้า") {
                                                onDismiss()
                                            }
                                        } else {
                                            val newStock = (mat.currentStock - delta).coerceAtLeast(0.0)
                                            viewModel.adjustMaterialStock(mat.id, newStock, "สแกนบาร์โค้ดตัดยอดเบิกใช้") {
                                                onDismiss()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAdd) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isAdd) "เพิ่มเข้า" else "ตัดออก")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 2 Quick Actions: Restock (+) or Deduct (-)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { quickActionType = "ADD" },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("scan_action_restock")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ รับเข้าสต็อก", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { quickActionType = "REMOVE" },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("scan_action_remove_material")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("- ตัดยอดใช้งาน", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                // CASE 3: NOT FOUND IN DATABASE
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ไม่พบรายการที่มีรหัสนี้ในระบบ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "รหัส: $scannedCode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("ต้องการผูกรหัสนี้กับอะไร?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onDismiss()
                            onAddProductWithCode(scannedCode)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ สร้างสินค้าใหม่ด้วยรหัสนี้")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onAddMaterialWithCode(scannedCode)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ สร้างวัตถุดิบใหม่ด้วยรหัสนี้")
                    }
                }
            }
        }
    }
}
