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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ProductWithRecipeDetails
import com.example.ui.InventoryViewModel
import com.example.ui.components.numberFormatter
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockOkColor

@Composable
fun BatchProductionDialog(
    productWithRecipe: ProductWithRecipeDetails,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(10) }
    var note by remember { mutableStateOf("") }

    val deductionResult by remember(quantity, productWithRecipe) {
        derivedStateOf {
            viewModel.calculateDeductionPreview(productWithRecipe, quantity)
        }
    }

    val product = productWithRecipe.product

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("batch_production_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ผลิตสินค้าเข้าสต็อก",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "จำนวนผลิตเสร็จ (ชิ้น):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    enabled = quantity > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "ลดจำนวน")
                                }
                                Text(
                                    text = "$quantity",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(
                                    onClick = { quantity++ },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "เพิ่มจำนวน",
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }

                        // Presets
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 20, 50, 100).forEach { preset ->
                                OutlinedButton(
                                    onClick = { quantity = preset },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    colors = if (quantity == preset) ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(text = "$preset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Materials required
                Text(
                    text = "ตัดยอดวัตถุดิบเข้าการผลิต (${productWithRecipe.ingredients.size} รายการ):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(deductionResult.previews) { item ->
                        val isDeficit = item.isShortage
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDeficit) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        text = "ใช้: ${numberFormatter.format(item.totalRequired)} ${item.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (isDeficit) "ขาด ${numberFormatter.format(-item.remainingStockAfter)} ${item.unit}"
                                    else "เหลือ ${numberFormatter.format(item.remainingStockAfter)} ${item.unit}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeficit) StockCriticalColor else StockOkColor
                                )
                            }
                        }
                    }
                }

                if (!deductionResult.canDeduct) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "วัตถุดิบไม่พอสำหรับการผลิตจำนวน $quantity ชิ้น",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("บันทึกแบทช์การผลิต (เช่น รอบเช้า, ล็อต 01)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.processProduction(
                            productWithRecipe = productWithRecipe,
                            quantity = quantity,
                            note = note,
                            onSuccess = onDismiss
                        )
                    },
                    enabled = deductionResult.canDeduct,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_produce_button")
                ) {
                    Text("ตัดวัตถุดิบ & เพิ่มสต็อกสินค้าสำเร็จรูป", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
