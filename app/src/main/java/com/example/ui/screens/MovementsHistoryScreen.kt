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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MovementType
import com.example.data.StockMovement
import com.example.ui.InventoryViewModel
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockOkColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovementsHistoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val movements by viewModel.filteredMovements.collectAsStateWithLifecycle()
    val currentFilter by viewModel.movementFilter.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header with Clear Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ประวัติการเคลื่อนไหวสต็อก",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "บันทึกการตัดยอดวัตถุดิบและรับเข้าทั้งหมด",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (movements.isNotEmpty()) {
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "ล้างประวัติ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        val filterOptions = listOf(
            null to "ทั้งหมด",
            MovementType.SALE_DEDUCTION to "ตัดยอดขาย",
            MovementType.PRODUCTION_DEDUCTION to "ตัดเพื่อผลิต",
            MovementType.RESTOCK_RAW to "รับเข้าวัตถุดิบ",
            MovementType.RESTOCK_PRODUCT to "รับเข้าสินค้า",
            MovementType.ADJUSTMENT to "ปรับยอด"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(filterOptions) { (type, label) ->
                FilterChip(
                    selected = currentFilter == type,
                    onClick = { viewModel.movementFilter.value = type },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        if (movements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ไม่มีประวัติการเคลื่อนไหว",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(movements, key = { it.id }) { movement ->
                    MovementItemCard(movement = movement)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("ยืนยันล้างประวัติ") },
            text = { Text("คุณต้องการลบประวัติการเคลื่อนไหวทั้งหมดใช่หรือไม่? ยอดสต็อกปัจจุบันจะไม่เปลี่ยนแปลง") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("ล้างประวัติ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun MovementItemCard(movement: StockMovement) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val timeFormatted = remember(movement.timestamp) { dateFormat.format(Date(movement.timestamp)) }

    val (badgeBg, badgeFg, icon) = when (movement.type) {
        MovementType.SALE_DEDUCTION -> Triple(
            StockCriticalColor.copy(alpha = 0.15f),
            StockCriticalColor,
            Icons.Default.ArrowDownward
        )
        MovementType.PRODUCTION_DEDUCTION -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.Build
        )
        MovementType.RESTOCK_RAW, MovementType.RESTOCK_PRODUCT -> Triple(
            StockOkColor.copy(alpha = 0.15f),
            StockOkColor,
            Icons.Default.ArrowUpward
        )
        MovementType.ADJUSTMENT -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Tune
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeFg,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movement.type.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = movement.referenceName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                if (movement.note.isNotBlank()) {
                    Text(
                        text = movement.note,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity delta
            Column(horizontalAlignment = Alignment.End) {
                val isPositive = movement.quantityDelta > 0
                val deltaPrefix = if (isPositive) "+" else ""
                Text(
                    text = "$deltaPrefix${numberFormatter.format(movement.quantityDelta)} ${movement.unit}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (isPositive) StockOkColor else StockCriticalColor
                )
                if (movement.totalCostOrPrice > 0) {
                    Text(
                        text = "${currencyFormatter.format(movement.totalCostOrPrice)} ฿",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
