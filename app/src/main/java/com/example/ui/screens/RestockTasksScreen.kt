package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.RawMaterial
import com.example.data.RestockTask
import com.example.ui.InventoryViewModel
import com.example.ui.components.currencyFormatter
import com.example.ui.components.numberFormatter
import com.example.ui.dialogs.AddRestockTaskDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RestockTasksScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allRestockTasks.collectAsStateWithLifecycle()
    val allRawMaterials by viewModel.allRawMaterials.collectAsStateWithLifecycle()
    val productsWithRecipes by viewModel.productsWithRecipes.collectAsStateWithLifecycle()
    val lowMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var targetMaterialForAdd by remember { mutableStateOf<RawMaterial?>(null) }
    var viewingImageUri by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, RAW_MATERIAL, PRODUCT

    val filteredTasks = tasks.filter { task ->
        val matchType = when (filterType) {
            "RAW_MATERIAL" -> task.itemType == "RAW_MATERIAL"
            "PRODUCT" -> task.itemType == "PRODUCT"
            else -> true
        }
        val matchQuery = searchQuery.isBlank() ||
                task.itemName.contains(searchQuery, ignoreCase = true) ||
                task.recordedBy.contains(searchQuery, ignoreCase = true) ||
                task.supplierOrSource.contains(searchQuery, ignoreCase = true) ||
                task.note.contains(searchQuery, ignoreCase = true)
        matchType && matchQuery
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("th", "TH")) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("restock_tasks_screen")
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "งานที่ต้องเพิ่มสต็อก",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "บันทึกงานรับเข้า แนบรูปภาพ & ระบุชื่อผู้เพิ่มสต็อก",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            targetMaterialForAdd = null
                            showAddDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_restock_task_header_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ บันทึกงานเพิ่มสต็อกใหม่ (แนบรูป)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Quick alert section if materials are running out
        if (lowMaterials.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "วัตถุดิบใกล้หมดที่ต้องเพิ่มสต็อกด่วน (${lowMaterials.size} รายการ)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        lowMaterials.take(3).forEach { mat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "คงเหลือ: ${numberFormatter.format(mat.currentStock)} / เกณฑ์: ${numberFormatter.format(mat.minStockThreshold)} ${mat.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        targetMaterialForAdd = mat
                                        showAddDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ เพิ่มสต็อก", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ค้นหาจากชื่อวัตถุดิบ หรือ ชื่อผู้เพิ่มสต็อก") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "ล้าง")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("ทั้งหมด (${tasks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = filterType == "RAW_MATERIAL",
                    onClick = { filterType = "RAW_MATERIAL" },
                    label = { Text("วัตถุดิบ (${tasks.count { it.itemType == "RAW_MATERIAL" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = filterType == "PRODUCT",
                    onClick = { filterType = "PRODUCT" },
                    label = { Text("สินค้าสำเร็จรูป (${tasks.count { it.itemType == "PRODUCT" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Empty state
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "ไม่พบรายการตามคำค้นหา" else "ยังไม่มีบันทึกงานเพิ่มสต็อก",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "กดปุ่ม '+ บันทึกงานเพิ่มสต็อก' เพื่อแนบรูปถ่ายและระบุชื่อผู้เพิ่ม",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Restock Task Cards
        items(filteredTasks, key = { it.id }) { task ->
            RestockTaskCard(
                task = task,
                dateFormatter = dateFormatter,
                onImageClick = { uri -> viewingImageUri = uri },
                onDelete = { viewModel.deleteRestockTask(task) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Add Restock Task Dialog
    if (showAddDialog) {
        AddRestockTaskDialog(
            initialMaterial = targetMaterialForAdd,
            allRawMaterials = allRawMaterials,
            allProducts = productsWithRecipes,
            viewModel = viewModel,
            onDismiss = {
                showAddDialog = false
                targetMaterialForAdd = null
            }
        )
    }

    // Fullscreen Attached Image Viewer Modal
    viewingImageUri?.let { uriStr ->
        Dialog(
            onDismissRequest = { viewingImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = Uri.parse(uriStr),
                    contentDescription = "รูปภาพแนบเต็มจอ",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                IconButton(
                    onClick = { viewingImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "ปิด", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun RestockTaskCard(
    task: RestockTask,
    dateFormatter: SimpleDateFormat,
    onImageClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("restock_task_item_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge + Timestamp + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (task.itemType == "RAW_MATERIAL") MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (task.itemType == "RAW_MATERIAL") "วัตถุดิบ: ${task.category}" else "สินค้า: ${task.category}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.itemType == "RAW_MATERIAL") MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormatter.format(Date(task.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "ลบรายการ",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Photo Thumbnail + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail if available
                if (!task.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { onImageClick(task.imageUri) }
                    ) {
                        AsyncImage(
                            model = Uri.parse(task.imageUri),
                            contentDescription = "รูปถ่ายสินค้าที่เพิ่มสต็อก",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(bottomStart = 6.dp),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "ดูรูป",
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "ไม่มีรูป",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "+ ${numberFormatter.format(task.quantityAdded)} ${task.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF16A34A) // Crisp Green
                    )

                    if (task.totalCost > 0) {
                        Text(
                            text = "ต้นทุน: ${currencyFormatter.format(task.totalCost)} ฿",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: *** ผู้เพิ่มสต็อก *** & Source
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name of person who added stock (PROMINENT)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ผู้เพิ่มสต็อก: ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = task.recordedBy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (task.supplierOrSource.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.supplierOrSource,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "หมายเหตุ: ${task.note}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
