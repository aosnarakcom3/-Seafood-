package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.ProductWithRecipeDetails
import com.example.ui.InventoryViewModel
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.dialogs.ProductRecipeDialog

@Composable
fun ProductsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allRawMaterials by viewModel.allRawMaterials.collectAsStateWithLifecycle()
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.productSelectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "สร้างสินค้า")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("สร้างสินค้า & สูตร", fontWeight = FontWeight.Bold)
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
                onValueChange = { viewModel.productSearchQuery.value = it },
                placeholder = { Text("ค้นหาสินค้า หรือ สูตรวัตถุดิบ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.productSelectedCategory.value = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "รายการสินค้าและสูตร BOM (${products.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ยังไม่มีสินค้าในระบบ",
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
                    items(products, key = { it.product.id }) { item ->
                        ProductRecipeCard(
                            item = item,
                            onEdit = { editingProduct = item },
                            onDelete = { deletingProduct = item }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        ProductRecipeDialog(
            initialProduct = null,
            allRawMaterials = allRawMaterials,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    editingProduct?.let { p ->
        ProductRecipeDialog(
            initialProduct = p,
            allRawMaterials = allRawMaterials,
            viewModel = viewModel,
            onDismiss = { editingProduct = null }
        )
    }

    deletingProduct?.let { p ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("ยืนยันการลบสินค้า") },
            text = { Text("คุณต้องการลบสินค้า '${p.product.name}' และสูตรวัตถุดิบทั้งหมดใช่หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(p.product)
                        deletingProduct = null
                    }
                ) {
                    Text("ลบสินค้า", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun ProductRecipeCard(
    item: ProductWithRecipeDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedRecipe by remember { mutableStateOf(false) }
    val product = item.product

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "แก้ไข", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "ลบ",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Name & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "คงเหลือ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val stockCount = if (product.trackFinishedStock) product.currentStock else item.maxProducibleCount
                    Text(
                        text = "$stockCount ชิ้น",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (stockCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary: Cost per piece & Margin
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ต้นทุนวัตถุดิบ/ชิ้น", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${currencyFormatter.format(item.totalCostPerUnit)} ฿",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("กำไรต่อชิ้น", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${currencyFormatter.format(item.grossProfitMargin)} ฿",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("สต็อกทำได้", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${item.maxProducibleCount} ชิ้น",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (item.maxProducibleCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Recipe BOM Ingredients
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "สูตรวัตถุดิบที่ใช้ (${item.ingredients.size} รายการ):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { expandedRecipe = !expandedRecipe }) {
                    Text(if (expandedRecipe) "ซ่อนสูตร" else "ดูสูตรละเอียด", fontSize = 12.sp)
                    Icon(
                        imageVector = if (expandedRecipe) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expandedRecipe) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    item.ingredients.forEach { ing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${ing.rawMaterialName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${numberFormatter.format(ing.quantityRequired)} ${ing.unit} (ต้นทุน ~${currencyFormatter.format(ing.estimatedItemCost)} ฿)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
