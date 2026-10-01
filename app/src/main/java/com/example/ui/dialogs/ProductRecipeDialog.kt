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
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.Product
import com.example.data.ProductWithRecipeDetails
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter

@Composable
fun ProductRecipeDialog(
    initialProduct: ProductWithRecipeDetails? = null,
    allRawMaterials: List<RawMaterial>,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.product?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.product?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.product?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.product?.category ?: "เครื่องดื่ม") }
    var priceStr by remember { mutableStateOf(initialProduct?.product?.price?.let { numberFormatter.format(it) } ?: "50") }
    var trackFinishedStock by remember { mutableStateOf(initialProduct?.product?.trackFinishedStock ?: false) }
    var currentStockStr by remember { mutableStateOf("${initialProduct?.product?.currentStock ?: 0}") }
    var minStockThresholdStr by remember { mutableStateOf("${initialProduct?.product?.minStockThreshold ?: 0}") }

    // Map: rawMaterialId -> quantityRequired per 1 product unit
    val recipeQuantities = remember {
        mutableStateMapOf<Long, Double>().apply {
            initialProduct?.ingredients?.forEach { ing ->
                put(ing.rawMaterialId, ing.quantityRequired)
            }
        }
    }

    var showMaterialPicker by remember { mutableStateOf(false) }

    // Live calculation of recipe cost
    val rawMaterialMap = remember(allRawMaterials) { allRawMaterials.associateBy { it.id } }
    val totalRecipeCost by remember(recipeQuantities, rawMaterialMap) {
        derivedStateOf {
            recipeQuantities.entries.sumOf { (matId, qty) ->
                val mat = rawMaterialMap[matId]
                (mat?.costPerUnit ?: 0.0) * qty
            }
        }
    }

    val sellingPrice = priceStr.replace(",", "").toDoubleOrNull() ?: 0.0
    val grossProfit = sellingPrice - totalRecipeCost

    val commonCategories = listOf("เครื่องดื่ม", "เบเกอรี่", "ของหวาน", "อาหาร", "สินค้าทั่วไป")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .testTag("product_recipe_dialog"),
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
                    Column {
                        Text(
                            text = if (initialProduct == null) "สร้างสินค้า & สูตรตัดสต็อก (BOM)" else "แก้ไขสินค้า & สูตรวัตถุดิบ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อสินค้า * (เช่น ชานมไข่มุก, ขนมปัง)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
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
                    label = { Text("หมวดหมู่สินค้า") },
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

                // Price & Track Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("ราคาขาย (บาท) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_price_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (trackFinishedStock) {
                        OutlinedTextField(
                            value = currentStockStr,
                            onValueChange = { currentStockStr = it },
                            label = { Text("สต็อกสำเร็จรูป") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (trackFinishedStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = minStockThresholdStr,
                        onValueChange = { minStockThresholdStr = it },
                        label = { Text("เตือนเมื่อสต็อกสินค้าต่ำกว่า (ชิ้น)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("นับสต็อกสำเร็จรูปพร้อมขาย", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("เปิดเมื่อผลิตสต็อกไว้ล่วงหน้า (เช่น เบเกอรี่/ขนม)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = trackFinishedStock, onCheckedChange = { trackFinishedStock = it })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- RECIPE (BOM) BUILDER SECTION ---
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                            Column {
                                Text(
                                    text = "สูตรตัดยอดวัตถุดิบ (BOM)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "วัตถุดิบที่ต้องใช้ต่อสินค้า 1 ชิ้น",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showMaterialPicker = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ ใส่วัตถุดิบ", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (recipeQuantities.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ยังไม่มีวัตถุดิบในสูตร\nกด '+ ใส่วัตถุดิบ' เพื่อเลือกวัตถุดิบที่ต้องตัดยอด",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            recipeQuantities.keys.forEach { matId ->
                                val mat = rawMaterialMap[matId]
                                if (mat != null) {
                                    val currentQty = recipeQuantities[matId] ?: 0.0
                                    var qtyText by remember(matId, currentQty) {
                                        mutableStateOf(numberFormatter.format(currentQty))
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = mat.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "สต็อกมี: ${numberFormatter.format(mat.currentStock)} ${mat.unit}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Quantity input
                                            OutlinedTextField(
                                                value = qtyText,
                                                onValueChange = {
                                                    qtyText = it
                                                    val parsed = it.replace(",", "").toDoubleOrNull() ?: 0.0
                                                    recipeQuantities[matId] = parsed
                                                },
                                                suffix = { Text(mat.unit, fontSize = 11.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                modifier = Modifier.width(110.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            IconButton(
                                                onClick = { recipeQuantities.remove(matId) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "ลบวัตถุดิบออกจากสูตร",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Cost & Margin Summary Box
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("ต้นทุนวัตถุดิบต่อชิ้น:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${currencyFormatter.format(totalRecipeCost)} ฿",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("กำไรขั้นต้นโดยประมาณ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${currencyFormatter.format(grossProfit)} ฿",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (grossProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("ยกเลิก")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val parsedPrice = priceStr.replace(",", "").toDoubleOrNull() ?: 0.0
                            val parsedStock = currentStockStr.replace(",", "").toIntOrNull() ?: 0
                            val parsedMinThreshold = minStockThresholdStr.replace(",", "").toIntOrNull() ?: 0

                            val product = Product(
                                id = initialProduct?.product?.id ?: 0L,
                                name = name.trim(),
                                sku = sku.trim(),
                                barcode = barcode.trim(),
                                category = category.trim().ifBlank { "ทั่วไป" },
                                price = parsedPrice,
                                currentStock = parsedStock,
                                trackFinishedStock = trackFinishedStock,
                                minStockThreshold = parsedMinThreshold
                            )
                            viewModel.saveProductWithRecipe(
                                product = product,
                                recipeQuantities = recipeQuantities,
                                onSuccess = onDismiss
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_product_recipe_button")
                    ) {
                        Text("บันทึกสินค้า & สูตร")
                    }
                }
            }
        }
    }

    // Material Picker Dialog
    if (showMaterialPicker) {
        Dialog(onDismissRequest = { showMaterialPicker = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "เลือกวัตถุดิบใส่ในสูตร",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val unselectedMaterials = allRawMaterials.filter { !recipeQuantities.containsKey(it.id) }

                    if (unselectedMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("คุณได้เพิ่มวัตถุดิบทั้งหมดที่มีในระบบเข้าสูตรแล้ว")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(unselectedMaterials) { mat ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = {
                                        recipeQuantities[mat.id] = 10.0 // Default 10 units
                                        showMaterialPicker = false
                                    },
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
                                            Text(mat.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "หมวด: ${mat.category} | มี: ${numberFormatter.format(mat.currentStock)} ${mat.unit}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "เลือก",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { showMaterialPicker = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("ปิด")
                    }
                }
            }
        }
    }
}
