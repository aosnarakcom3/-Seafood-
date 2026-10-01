package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RawMaterial
import com.example.ui.theme.StockCriticalColor
import com.example.ui.theme.StockLowColor
import com.example.ui.theme.StockOkColor
import java.text.DecimalFormat

val numberFormatter = DecimalFormat("#,##0.##")
val currencyFormatter = DecimalFormat("#,##0.00")

@Composable
fun StockStatusChip(
    currentStock: Double,
    minThreshold: Double,
    modifier: Modifier = Modifier
) {
    val (label, bg, fg) = when {
        currentStock <= 0.0 -> Triple("สต็อกหมด", StockCriticalColor.copy(alpha = 0.15f), StockCriticalColor)
        currentStock <= minThreshold && minThreshold > 0 -> Triple("ใกล้หมด", StockLowColor.copy(alpha = 0.15f), StockLowColor)
        else -> Triple("พร้อมใช้", StockOkColor.copy(alpha = 0.15f), StockOkColor)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LowStockAlertBanner(
    lowStockMaterials: List<RawMaterial>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lowStockMaterials.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewAllClick)
            .testTag("low_stock_banner")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "แจ้งเตือน",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "วัตถุดิบใกล้หมด ${lowStockMaterials.size} รายการ!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = lowStockMaterials.take(3).joinToString(", ") { it.name } +
                            if (lowStockMaterials.size > 3) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "ดูรายการ >",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
