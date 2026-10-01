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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ProductWithRecipeDetails
import com.example.ui.InventoryViewModel
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.LowStockAlertBanner
import com.example.ui.components.StatCard
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.dialogs.BatchProductionDialog
import com.example.ui.dialogs.DeductStockDialog
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockOkColor

@Composable
fun QuickDeductScreen(
    viewModel: InventoryViewModel,
    onNavigateToRawMaterials: () -> Unit,
    onNavigateToAlerts: () -> Unit = onNavigateToRawMaterials,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allRawMaterials by viewModel.allRawMaterials.collectAsStateWithLifecycle()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.productSelectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()

    var selectedForDeduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }
    var selectedForProduction by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Low stock warning banner if any raw materials are low
        LowStockAlertBanner(
            lowStockMaterials = lowStockMaterials,
            onViewAllClick = onNavigateToAlerts,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "สินค้าพร้อมขาย",
                value = "${products.size}",
                subtitle = "มีสูตรตัดยอดวัตถุดิบ",
                icon = Icons.Default.PointOfSale,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "วัตถุดิบคลัง",
                value = "${allRawMaterials.size}",
                subtitle = if (lowStockMaterials.isNotEmpty()) "ใกล้หมด ${lowStockMaterials.size}" else "สต็อกเพียงพอ",
                icon = Icons.Default.Inventory2,
                containerColor = if (lowStockMaterials.isNotEmpty())
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                iconTint = if (lowStockMaterials.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.productSearchQuery.value = it },
            placeholder = { Text("ค้นหาสินค้าที่ต้องการตัดสต็อก/ขาย...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_product_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category filter chips
        CategoryFilterRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.productSelectedCategory.value = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Product Cards List
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ไม่พบรายการสินค้า",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products, key = { it.product.id }) { item ->
                    ProductDeductCard(
                        item = item,
                        onDeductClick = { selectedForDeduct = item },
                        onProduceClick = { selectedForProduction = item }
                    )
                }
            }
        }
    }

    // Active Dialogs
    selectedForDeduct?.let { p ->
        DeductStockDialog(
            productWithRecipe = p,
            viewModel = viewModel,
            onDismiss = { selectedForDeduct = null }
        )
    }

    selectedForProduction?.let { p ->
        BatchProductionDialog(
            productWithRecipe = p,
            viewModel = viewModel,
            onDismiss = { selectedForProduction = null }
        )
    }
}

@Composable
fun ProductDeductCard(
    item: ProductWithRecipeDetails,
    onDeductClick: () -> Unit,
    onProduceClick: () -> Unit
) {
    val product = item.product
    val maxProducible = item.maxProducibleCount
    val canMake = maxProducible > 0

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        if (product.sku.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = product.sku,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "คงเหลือ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val stockCount = if (product.trackFinishedStock) product.currentStock else maxProducible
                    Text(
                        text = "$stockCount ชิ้น",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (stockCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock & Producibility info
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
                // How many can be made from raw materials
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (canMake) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (canMake) StockOkColor else StockCriticalColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canMake) "วัตถุดิบพอทำได้: $maxProducible ชิ้น" else "วัตถุดิบไม่พอ!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canMake) StockOkColor else StockCriticalColor
                    )
                }

                // Finished stock if tracked
                if (product.trackFinishedStock) {
                    Text(
                        text = "สต็อกพร้อมขาย: ${product.currentStock} ชิ้น",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "สูตร: ${item.ingredients.size} วัตถุดิบ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Ingredients badge preview
            if (item.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ตัดวัตถุดิบ: " + item.ingredients.joinToString(", ") { "${it.rawMaterialName} (${numberFormatter.format(it.quantityRequired)}${it.unit})" },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Deduct / Sell & Optional Produce
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.trackFinishedStock) {
                    OutlinedButton(
                        onClick = onProduceClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ผลิตเข้าสต็อก", fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onDeductClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("deduct_button_${product.id}")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ตัดสต็อก / ขาย", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
