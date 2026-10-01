package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MovementType
import com.example.data.StockMovement
import com.example.ui.InventoryViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val title: String, val days: Int) {
    DAILY("รายวัน (วันนี้)", 1),
    WEEKLY("รายสัปดาห์ (7 วัน)", 7),
    MONTHLY("รายเดือน (30 วัน)", 30)
}

data class ProductSaleSummary(
    val productName: String,
    val totalQuantitySold: Double,
    val totalRevenue: Double
)

data class MaterialConsumptionSummary(
    val materialName: String,
    val totalQuantityConsumed: Double,
    val unit: String,
    val totalCost: Double
)

data class DailyChartPoint(
    val label: String,
    val revenue: Double,
    val cost: Double
)

@Composable
fun ReportsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.WEEKLY) }
    val movements by viewModel.movements.collectAsStateWithLifecycle()

    // Calculate cutoff timestamp based on selected period
    val periodReport by remember(movements, selectedPeriod) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            val cutoff = when (selectedPeriod) {
                ReportPeriod.DAILY -> {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }
                ReportPeriod.WEEKLY -> now - (7L * 24 * 3600 * 1000)
                ReportPeriod.MONTHLY -> now - (30L * 24 * 3600 * 1000)
            }

            val periodMovements = movements.filter { it.timestamp >= cutoff }

            // 1. Sales movements (Product deductions)
            val saleMovements = periodMovements.filter {
                it.type == MovementType.SALE_DEDUCTION && it.totalCostOrPrice > 0 && !it.referenceName.contains("จากขาย")
            }
            val totalRevenue = saleMovements.sumOf { it.totalCostOrPrice }
            val totalSalesCount = saleMovements.sumOf { -it.quantityDelta }

            // 2. Raw Material Consumption movements
            val rawConsumptionMovements = periodMovements.filter {
                (it.type == MovementType.SALE_DEDUCTION || it.type == MovementType.PRODUCTION_DEDUCTION) &&
                        it.quantityDelta < 0 && (it.referenceName.contains("จากขาย") || it.referenceName.contains("ใช้ผลิต"))
            }
            val totalMaterialCost = rawConsumptionMovements.sumOf { it.totalCostOrPrice }

            // Top Products Sold
            val topProducts = saleMovements.groupBy { it.referenceName }
                .map { (name, list) ->
                    ProductSaleSummary(
                        productName = name,
                        totalQuantitySold = list.sumOf { -it.quantityDelta },
                        totalRevenue = list.sumOf { it.totalCostOrPrice }
                    )
                }.sortedByDescending { it.totalRevenue }

            // Raw Material Consumption Breakdown
            val materialBreakdown = rawConsumptionMovements.groupBy {
                // Strip description parentheses to group by clean material name
                it.referenceName.substringBefore(" (")
            }.map { (name, list) ->
                MaterialConsumptionSummary(
                    materialName = name,
                    totalQuantityConsumed = list.sumOf { -it.quantityDelta },
                    unit = list.firstOrNull()?.unit ?: "หน่วย",
                    totalCost = list.sumOf { it.totalCostOrPrice }
                )
            }.sortedByDescending { it.totalCost }

            // Chart points: group by date
            val daysCount = selectedPeriod.days
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            val chartPoints = (0 until daysCount.coerceAtMost(7)).reversed().map { dayOffset ->
                val dayStart = now - (dayOffset * 24L * 3600 * 1000)
                val dayDate = Date(dayStart)
                val dayLabel = sdf.format(dayDate)
                val startWindow = dayStart - (12L * 3600 * 1000)
                val endWindow = dayStart + (12L * 3600 * 1000)

                val daySales = saleMovements.filter { it.timestamp in startWindow..endWindow }
                val dayCost = rawConsumptionMovements.filter { it.timestamp in startWindow..endWindow }
                DailyChartPoint(
                    label = dayLabel,
                    revenue = daySales.sumOf { it.totalCostOrPrice },
                    cost = dayCost.sumOf { it.totalCostOrPrice }
                )
            }

            object {
                val revenue = totalRevenue
                val materialCost = totalMaterialCost
                val profit = totalRevenue - totalMaterialCost
                val salesCount = totalSalesCount.toInt()
                val products = topProducts
                val materials = materialBreakdown
                val charts = chartPoints
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Period Filter Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Summary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "ยอดขายรวม",
                    value = "${currencyFormatter.format(periodReport.revenue)} ฿",
                    subtitle = "ขายได้ ${periodReport.salesCount} ออเดอร์",
                    icon = Icons.Default.PointOfSale,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "ต้นทุนวัตถุดิบ",
                    value = "${currencyFormatter.format(periodReport.materialCost)} ฿",
                    subtitle = "ตัดยอดตามสูตร BOM",
                    icon = Icons.Default.Kitchen,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gross Profit Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "กำไรขั้นต้นสุทธิ (Gross Profit)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currencyFormatter.format(periodReport.profit)} ฿",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Visual Sales & Cost Bar Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "กราฟแสดงยอดขายและต้นทุน",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ยอดขาย", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ต้นทุน", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val maxVal = (periodReport.charts.maxOfOrNull { maxOf(it.revenue, it.cost) } ?: 100.0).coerceAtLeast(100.0)

                    // Compose Canvas Bar Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barWidth = 14.dp.toPx()
                            val spacing = w / (periodReport.charts.size.coerceAtLeast(1) + 1)

                            // Base horizontal line
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, h - 20.dp.toPx()),
                                end = Offset(w, h - 20.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )

                            periodReport.charts.forEachIndexed { i, point ->
                                val cx = (i + 1) * spacing
                                val availableHeight = h - 30.dp.toPx()

                                // Revenue Bar (Primary)
                                val revHeight = (point.revenue / maxVal * availableHeight).toFloat()
                                drawRoundRect(
                                    color = Color(0xFF1D4ED8),
                                    topLeft = Offset(cx - barWidth - 2.dp.toPx(), h - 20.dp.toPx() - revHeight),
                                    size = Size(barWidth, revHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                // Cost Bar (Tertiary)
                                val costHeight = (point.cost / maxVal * availableHeight).toFloat()
                                drawRoundRect(
                                    color = Color(0xFFD97706),
                                    topLeft = Offset(cx + 2.dp.toPx(), h - 20.dp.toPx() - costHeight),
                                    size = Size(barWidth, costHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }

                        // Labels row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            periodReport.charts.forEach { point ->
                                Text(
                                    text = point.label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Selling Products
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "อันดับสินค้าขายดี (ยอดขาย)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (periodReport.products.isEmpty()) {
                        Text(
                            text = "ยังไม่มีประวัติการขายในช่วงเวลานี้",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxRevenue = periodReport.products.maxOfOrNull { it.totalRevenue } ?: 1.0
                        periodReport.products.take(5).forEachIndexed { index, product ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${index + 1}. ${product.productName}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${currencyFormatter.format(product.totalRevenue)} ฿ (${product.totalQuantitySold.toInt()} ชิ้น)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (product.totalRevenue / maxRevenue).toFloat() },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Raw Material Consumption Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "สรุปการตัดยอดและใช้วัตถุดิบ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (periodReport.materials.isEmpty()) {
                        Text(
                            text = "ยังไม่มีการใช้วัตถุดิบในช่วงเวลานี้",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxMatCost = periodReport.materials.maxOfOrNull { it.totalCost } ?: 1.0
                        periodReport.materials.forEach { mat ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mat.materialName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${numberFormatter.format(mat.totalQuantityConsumed)} ${mat.unit} (~${currencyFormatter.format(mat.totalCost)} ฿)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (mat.totalCost / maxMatCost).toFloat() },
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
