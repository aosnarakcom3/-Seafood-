package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.ProductWithRecipeDetails
import com.example.data.RawMaterial
import com.example.data.RestockTask
import com.example.ui.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRestockTaskDialog(
    initialMaterial: RawMaterial? = null,
    allRawMaterials: List<RawMaterial>,
    allProducts: List<ProductWithRecipeDetails>,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    // Mode: "RAW_MATERIAL" or "PRODUCT"
    var itemType by remember { mutableStateOf(if (initialMaterial != null) "RAW_MATERIAL" else "RAW_MATERIAL") }

    var selectedMaterial by remember { mutableStateOf(initialMaterial ?: allRawMaterials.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(allProducts.firstOrNull()) }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("") }
    var recordedBy by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var supplierOrSource by remember { mutableStateOf("") }
    var totalCostStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker for attaching image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val currentUnit = if (itemType == "RAW_MATERIAL") {
        selectedMaterial?.unit ?: "หน่วย"
    } else {
        "ชิ้น"
    }

    val currentName = if (itemType == "RAW_MATERIAL") {
        selectedMaterial?.name ?: ""
    } else {
        selectedProduct?.product?.name ?: ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("add_restock_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "บันทึกงานเพิ่มสต็อก",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "แนบรูปภาพ พร้อมระบุชื่อผู้เพิ่มสต็อก",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type selector: วัตถุดิบ vs สินค้าสำเร็จรูป
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { itemType = "RAW_MATERIAL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (itemType == "RAW_MATERIAL") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (itemType == "RAW_MATERIAL") MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("วัตถุดิบ (Raw Material)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { itemType = "PRODUCT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (itemType == "PRODUCT") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (itemType == "PRODUCT") MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("สินค้าสำเร็จรูป (Product)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (itemType == "RAW_MATERIAL") "เลือกวัตถุดิบที่เพิ่มสต็อก *" else "เลือกสินค้าสำเร็จรูปที่เพิ่มสต็อก *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        if (itemType == "RAW_MATERIAL") {
                            allRawMaterials.forEach { mat ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(mat.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "หมวด: ${mat.category} | คงเหลือปัจจุบัน: ${mat.currentStock} ${mat.unit}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedMaterial = mat
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        } else {
                            allProducts.forEach { p ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(p.product.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "หมวด: ${p.product.category} | สต็อกปัจจุบัน: ${p.product.currentStock} ชิ้น",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProduct = p
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Added
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("จำนวนที่เพิ่มเข้าสต็อก ($currentUnit) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_quantity_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // *** CRITICAL REQUIREMENT: ชื่อผู้เพิ่มสต็อก ***
                OutlinedTextField(
                    value = recordedBy,
                    onValueChange = { recordedBy = it },
                    label = { Text("ชื่อผู้เพิ่มสต็อก (เช่น สมชาย, พี่กบ, นก) *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_recorded_by_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Staff Name Suggestion Chips
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("สมชาย (ผจก.)", "แนน (สต็อก)", "บอย (ครัว)", "กะเช้า", "กะเย็น").forEach { name ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { recordedBy = name }
                        ) {
                            Text(
                                text = "+ $name",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // *** CRITICAL REQUIREMENT: แนบรูปภาพ ***
                Text(
                    text = "แนบรูปภาพ (รูปสินค้า / ใบเสร็จ / บิลส่งของ)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "รูปภาพแนบ",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Remove image button
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "ลบรูป", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .testTag("attach_photo_button")
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("กดเพื่อเลือกรูปภาพแนบ", fontWeight = FontWeight.Bold)
                            Text("เลือกจากแกลเลอรี หรือรูปถ่ายสินค้า/ใบเสร็จ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Supplier / Store (Optional)
                OutlinedTextField(
                    value = supplierOrSource,
                    onValueChange = { supplierOrSource = it },
                    label = { Text("ร้านค้า / แหล่งที่มา (เช่น แม็คโคร, เจริญผล)") },
                    leadingIcon = {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Total Cost (Optional)
                OutlinedTextField(
                    value = totalCostStr,
                    onValueChange = { totalCostStr = it },
                    label = { Text("ยอดเงินรวม (บาท) ถ้ามี") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("หมายเหตุเพิ่มเติม (เช่น เลขที่บิล, ล็อตสินค้า)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Error message display
                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ยกเลิก")
                    }

                    Button(
                        onClick = {
                            val qty = quantityStr.toDoubleOrNull()
                            if (qty == null || qty <= 0) {
                                errorMessage = "กรุณาระบุจำนวนที่เพิ่มให้ถูกต้องและมากกว่า 0"
                                return@Button
                            }
                            if (recordedBy.isBlank()) {
                                errorMessage = "กรุณาระบุชื่อผู้เพิ่มสต็อก"
                                return@Button
                            }

                            val task = if (itemType == "RAW_MATERIAL") {
                                val mat = selectedMaterial
                                if (mat == null) {
                                    errorMessage = "กรุณาเลือกวัตถุดิบ"
                                    return@Button
                                }
                                RestockTask(
                                    itemType = "RAW_MATERIAL",
                                    itemId = mat.id,
                                    itemName = mat.name,
                                    category = mat.category,
                                    quantityAdded = qty,
                                    unit = mat.unit,
                                    recordedBy = recordedBy.trim(),
                                    imageUri = selectedImageUri?.toString(),
                                    supplierOrSource = supplierOrSource.trim(),
                                    totalCost = totalCostStr.toDoubleOrNull() ?: 0.0,
                                    note = note.trim()
                                )
                            } else {
                                val p = selectedProduct
                                if (p == null) {
                                    errorMessage = "กรุณาเลือกสินค้า"
                                    return@Button
                                }
                                RestockTask(
                                    itemType = "PRODUCT",
                                    itemId = p.product.id,
                                    itemName = p.product.name,
                                    category = p.product.category,
                                    quantityAdded = qty,
                                    unit = "ชิ้น",
                                    recordedBy = recordedBy.trim(),
                                    imageUri = selectedImageUri?.toString(),
                                    supplierOrSource = supplierOrSource.trim(),
                                    totalCost = totalCostStr.toDoubleOrNull() ?: 0.0,
                                    note = note.trim()
                                )
                            }

                            viewModel.submitRestockTask(task, onSuccess = onDismiss)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_restock_task_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ยืนยันเพิ่มสต็อก", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
