package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ProductWithRecipeDetails
import com.example.ui.InventoryViewModel
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockOkColor

@Composable
fun DeductStockDialog(
    productWithRecipe: ProductWithRecipeDetails,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }
    var forceAllowDeficit by remember { mutableStateOf(false) }

    val deductionResult by remember(quantity, productWithRecipe) {
        derivedStateOf {
            viewModel.calculateDeductionPreview(productWithRecipe, quantity)
        }
    }

    val product = productWithRecipe.product
    val totalPrice = product.price * quantity

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("deduct_stock_dialog"),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ตัดสต็อก / ขายสินค้า",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity selector
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
                                text = "จำนวนที่ขาย:",
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
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "เพิ่มจำนวน",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        // Quick presets chips
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 5, 10).forEach { preset ->
                                OutlinedButton(
                                    onClick = { quantity = preset },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    colors = if (quantity == preset) ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(text = "$preset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recipe BOM Deduction Preview Table
                Text(
                    text = "รายการวัตถุดิบที่จะถูกตัดยอด (${productWithRecipe.ingredients.size} รายการ):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (productWithRecipe.ingredients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "สินค้านี้ยังไม่ได้ผูกสูตรวัตถุดิบ (BOM)\nสามารถเพิ่มสูตรได้ในแท็บ 'สินค้า & สูตร'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                } else {
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
                                border = if (isDeficit) androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error
                                ) else null,
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
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "ตัด: ${numberFormatter.format(item.totalRequired)} ${item.unit} (${numberFormatter.format(item.requiredPerUnit)} /ชิ้น)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isDeficit) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isDeficit) StockCriticalColor else StockOkColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isDeficit) "ขาด ${numberFormatter.format(-item.remainingStockAfter)} ${item.unit}"
                                                else "เหลือ ${numberFormatter.format(item.remainingStockAfter)} ${item.unit}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDeficit) StockCriticalColor else StockOkColor
                                            )
                                        }
                                        Text(
                                            text = "เดิม: ${numberFormatter.format(item.currentStock)} ${item.unit}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Shortage Alert Box if any deficit
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
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "สต็อกวัตถุดิบไม่เพียงพอสำหรับ $quantity ชิ้น",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "ทำได้สูงสุด ${productWithRecipe.maxProducibleCount} ชิ้นจากสต็อกปัจจุบัน",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("บันทึกเพิ่มเติม (เช่น โต๊ะ, ออเดอร์, Grab)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom summary & Confirm Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "คงเหลือหลังตัด",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val remainingStock = if (product.trackFinishedStock) {
                            "${(product.currentStock - quantity).coerceAtLeast(0)} ชิ้น"
                        } else {
                            "${(productWithRecipe.maxProducibleCount - quantity).coerceAtLeast(0)} ชิ้น"
                        }
                        Text(
                            text = remainingStock,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.processSale(
                                productWithRecipe = productWithRecipe,
                                quantity = quantity,
                                note = note,
                                forceAllowDeficit = forceAllowDeficit,
                                onSuccess = onDismiss
                            )
                        },
                        enabled = deductionResult.canDeduct || forceAllowDeficit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("confirm_deduct_button")
                    ) {
                        Text(
                            text = "ยืนยันตัดยอดวัตถุดิบ",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
