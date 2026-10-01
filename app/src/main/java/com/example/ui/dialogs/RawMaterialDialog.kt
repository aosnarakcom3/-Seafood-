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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.numberFormatter

@Composable
fun AddEditRawMaterialDialog(
    initialMaterial: RawMaterial? = null,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var sku by remember { mutableStateOf(initialMaterial?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialMaterial?.barcode ?: "") }
    var category by remember { mutableStateOf(initialMaterial?.category ?: "ทั่วไป") }
    var currentStock by remember { mutableStateOf(initialMaterial?.currentStock?.let { numberFormatter.format(it) } ?: "0") }
    var unit by remember { mutableStateOf(initialMaterial?.unit ?: "กรัม") }
    var minThreshold by remember { mutableStateOf(initialMaterial?.minStockThreshold?.let { numberFormatter.format(it) } ?: "0") }
    var costPerUnit by remember { mutableStateOf(initialMaterial?.costPerUnit?.let { numberFormatter.format(it) } ?: "0") }

    val commonUnits = listOf("กรัม", "มล.", "ชิ้น", "กก.", "ลิตร", "ฟอง", "ถุง", "ชุด", "กล่อง")
    val commonCategories = listOf("ชาและกาแฟ", "นมและเนย", "ท็อปปิ้ง", "เบเกอรี่", "สารให้ความหวาน", "บรรจุภัณฑ์", "ทั่วไป")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("raw_material_form_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialMaterial == null) "เพิ่มวัตถุดิบใหม่" else "แก้ไขข้อมูลวัตถุดิบ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อวัตถุดิบ * (เช่น ใบชา, นมสด)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("material_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SKU & Barcode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("รหัส SKU") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("บาร์โค้ด / QR Code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("หมวดหมู่วัตถุดิบ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category chips
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonCategories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unit selection
                Text(
                    text = "หน่วยนับ *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonUnits) { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current stock & Min threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("สต็อกปัจจุบัน ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("material_stock_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = minThreshold,
                        onValueChange = { minThreshold = it },
                        label = { Text("เตือนเมื่อต่ำกว่า ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cost per unit
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("ต้นทุนต่อ 1 $unit (บาท)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ยกเลิก")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val parsedStock = currentStock.replace(",", "").toDoubleOrNull() ?: 0.0
                            val parsedMin = minThreshold.replace(",", "").toDoubleOrNull() ?: 0.0
                            val parsedCost = costPerUnit.replace(",", "").toDoubleOrNull() ?: 0.0

                            val material = RawMaterial(
                                id = initialMaterial?.id ?: 0L,
                                name = name.trim(),
                                sku = sku.trim(),
                                barcode = barcode.trim(),
                                category = category.trim().ifBlank { "ทั่วไป" },
                                currentStock = parsedStock,
                                unit = unit.trim().ifBlank { "กรัม" },
                                minStockThreshold = parsedMin,
                                costPerUnit = parsedCost
                            )
                            viewModel.saveRawMaterial(material, onSuccess = onDismiss)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_material_button")
                    ) {
                        Text("บันทึก")
                    }
                }
            }
        }
    }
}

/**
 * Restock Raw Material Modal (+ รับเข้าสต็อก)
 */
@Composable
fun RestockMaterialDialog(
    material: RawMaterial,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var addedQty by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "รับเข้าสต็อก (+)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = material.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("คงเหลือปัจจุบัน:", fontWeight = FontWeight.Medium)
                        Text(
                            "${numberFormatter.format(material.currentStock)} ${material.unit}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = addedQty,
                    onValueChange = { addedQty = it },
                    label = { Text("จำนวนที่รับเข้า (${material.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_quantity_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = totalCost,
                    onValueChange = { totalCost = it },
                    label = { Text("ราคารวม (บาท, ถ้ามี)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("หมายเหตุ / ผู้จำหน่าย / ล็อต") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("ยกเลิก")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = addedQty.replace(",", "").toDoubleOrNull() ?: 0.0
                            val cost = totalCost.replace(",", "").toDoubleOrNull() ?: 0.0
                            viewModel.restockMaterial(
                                rawMaterialId = material.id,
                                addedQuantity = qty,
                                cost = cost,
                                note = note,
                                onSuccess = onDismiss
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_restock_button")
                    ) {
                        Text("บันทึกรับเข้า")
                    }
                }
            }
        }
    }
}

/**
 * Adjust Raw Material Dialog (ปรับปรุงสต็อกตรวจนับ/เสียหาย)
 */
@Composable
fun AdjustMaterialDialog(
    material: RawMaterial,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var newStockStr by remember { mutableStateOf(numberFormatter.format(material.currentStock)) }
    var reason by remember { mutableStateOf("นับสต็อกจริง") }

    val commonReasons = listOf("นับสต็อกจริง", "ของเสีย/หมดอายุ", "ทำหก/แตกหัก", "ปรับแก้ข้อมูลผิด")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ปรับปรุงยอดสต็อก",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newStockStr,
                    onValueChange = { newStockStr = it },
                    label = { Text("ยอดสต็อกที่ถูกต้องใหม่ (${material.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("เหตุผลการปรับปรุง:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonReasons) { r ->
                        FilterChip(
                            selected = reason == r,
                            onClick = { reason = r },
                            label = { Text(r, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("ยกเลิก")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newStock = newStockStr.replace(",", "").toDoubleOrNull() ?: material.currentStock
                            viewModel.adjustMaterialStock(
                                rawMaterialId = material.id,
                                newStock = newStock,
                                reason = reason,
                                onSuccess = onDismiss
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("บันทึกปรับยอด")
                    }
                }
            }
        }
    }
}
