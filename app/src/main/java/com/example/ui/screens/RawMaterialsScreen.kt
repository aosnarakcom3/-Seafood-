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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RawMaterial
import com.example.ui.InventoryViewModel
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.StockStatusChip
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.dialogs.AddEditRawMaterialDialog
import com.example.ui.dialogs.AdjustMaterialDialog
import com.example.ui.dialogs.RestockMaterialDialog

@Composable
fun RawMaterialsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.filteredRawMaterials.collectAsStateWithLifecycle()
    val categories by viewModel.rawMaterialCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.rawMaterialSelectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.rawMaterialSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var restockingMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var adjustingMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var deletingMaterial by remember { mutableStateOf<RawMaterial?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_raw_material_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "เพิ่มวัตถุดิบ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เพิ่มวัตถุดิบ", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.rawMaterialSearchQuery.value = it },
                placeholder = { Text("ค้นหาชื่อวัตถุดิบ หรือ SKU...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_material_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category filter chips
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.rawMaterialSelectedCategory.value = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Count summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการวัตถุดิบทั้งหมด (${materials.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (materials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ไม่พบรายการวัตถุดิบ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(materials, key = { it.id }) { material ->
                        RawMaterialCard(
                            material = material,
                            onRestock = { restockingMaterial = material },
                            onAdjust = { adjustingMaterial = material },
                            onEdit = { editingMaterial = material },
                            onDelete = { deletingMaterial = material }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddEditRawMaterialDialog(
            initialMaterial = null,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    editingMaterial?.let { mat ->
        AddEditRawMaterialDialog(
            initialMaterial = mat,
            viewModel = viewModel,
            onDismiss = { editingMaterial = null }
        )
    }

    restockingMaterial?.let { mat ->
        RestockMaterialDialog(
            material = mat,
            viewModel = viewModel,
            onDismiss = { restockingMaterial = null }
        )
    }

    adjustingMaterial?.let { mat ->
        AdjustMaterialDialog(
            material = mat,
            viewModel = viewModel,
            onDismiss = { adjustingMaterial = null }
        )
    }

    deletingMaterial?.let { mat ->
        AlertDialog(
            onDismissRequest = { deletingMaterial = null },
            title = { Text("ยืนยันการลบวัตถุดิบ") },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบ '${mat.name}' ออกจากระบบ? รายการสูตรที่ผูกกับวัตถุดิบนี้อาจได้รับผลกระทบ") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRawMaterial(mat)
                        deletingMaterial = null
                    }
                ) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMaterial = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun RawMaterialCard(
    material: RawMaterial,
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("raw_material_card_${material.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category chip, Name, Status, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = material.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (material.sku.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = material.sku,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockStatusChip(
                        currentStock = material.currentStock,
                        minThreshold = material.minStockThreshold
                    )
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "ตัวเลือก")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("แก้ไขข้อมูล") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("ปรับปรุงสต็อก (ตรวจนับ)") },
                                onClick = {
                                    showMenu = false
                                    onAdjust()
                                },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("ลบวัตถุดิบ", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Material Name
            Text(
                text = material.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Level Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "คงเหลือในสต็อก",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = numberFormatter.format(material.currentStock),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (material.isOutOfStock) MaterialTheme.colorScheme.error
                            else if (material.isLowStock) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = material.unit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (material.minStockThreshold > 0) {
                        Text(
                            text = "เตือนเมื่อต่ำกว่า: ${numberFormatter.format(material.minStockThreshold)} ${material.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (material.costPerUnit > 0) {
                        Text(
                            text = "ต้นทุน: ${currencyFormatter.format(material.costPerUnit)} ฿/${material.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Restock (+) button & Adjust
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ปรับยอด", fontSize = 12.sp)
                }

                Button(
                    onClick = onRestock,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("restock_btn_${material.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ รับเข้าสต็อก", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
