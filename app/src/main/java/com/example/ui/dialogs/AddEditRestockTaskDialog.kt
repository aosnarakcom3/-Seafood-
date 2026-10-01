package com.example.ui.dialogs

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.RawMaterial
import com.example.data.RestockStatus
import com.example.data.RestockTask
import com.example.ui.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRestockTaskDialog(
    initialTask: RestockTask? = null,
    allRawMaterials: List<RawMaterial> = emptyList(),
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var quantityStr by remember { mutableStateOf(initialTask?.quantity?.let { if (it > 0) it.toString() else "" } ?: "") }
    var unit by remember { mutableStateOf(initialTask?.unit ?: "กก.") }
    var restockerName by remember { mutableStateOf(initialTask?.restockerName ?: "") }
    var estimatedCostStr by remember { mutableStateOf(initialTask?.estimatedCost?.let { if (it > 0) it.toString() else "" } ?: "") }
    var note by remember { mutableStateOf(initialTask?.note ?: "") }
    var selectedRawMaterialId by remember { mutableStateOf(initialTask?.rawMaterialId) }
    var imageUriString by remember { mutableStateOf(initialTask?.imageUri) }

    var isSaving by remember { mutableStateOf(false) }

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = copyUriToInternalStorage(context, uri)
                if (savedPath != null) {
                    imageUriString = savedPath
                }
            }
        }
    }

    val units = listOf("กก.", "กรัม", "ลิตร", "มล.", "ชิ้น", "ถุง", "กล่อง", "แพ็ค")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "สร้างงานที่ต้องเพิ่มสต็อก" else "แก้ไขงานเพิ่มสต็อก",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Quick select from existing raw materials
                if (allRawMaterials.isNotEmpty()) {
                    Text(
                        text = "เลือกจากรายการวัตถุดิบ (หรือพิมพ์ชื่อเองด้านล่าง):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allRawMaterials.take(4).forEach { mat ->
                            FilterChip(
                                selected = selectedRawMaterialId == mat.id,
                                onClick = {
                                    selectedRawMaterialId = mat.id
                                    title = mat.name
                                    unit = mat.unit
                                },
                                label = { Text(mat.name, fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Item Name
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ชื่อรายการที่ต้องเพิ่มสต็อก * (เช่น กุ้งแชบ๊วย, ปลาหมึก)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_task_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity and Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("จำนวนที่ต้องเพิ่ม *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restock_task_quantity_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("หน่วย") },
                        singleLine = true,
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Unit suggestion chips
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    units.take(5).forEach { u ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (unit == u) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { unit = u }
                        ) {
                            Text(
                                text = u,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (unit == u) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Restocker Name (ชื่อผู้เพิ่มสต็อก)
                OutlinedTextField(
                    value = restockerName,
                    onValueChange = { restockerName = it },
                    label = { Text("ชื่อผู้เพิ่มสต็อก / ผู้ปฏิบัติงาน *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    placeholder = { Text("เช่น สมชาย, กานดา, อุดมซีฟู้ด") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restock_task_restocker_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Image Attachment Section (แนบรูปภาพ)
                Text(
                    text = "แนบรูปภาพ (ใบเสร็จ, สภาพวัตถุดิบ หรือบิล):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (imageUriString != null) {
                    // Preview attached photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = File(imageUriString!!),
                            contentDescription = "รูปภาพแนบการเพิ่มสต็อก",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Remove image button
                        IconButton(
                            onClick = { imageUriString = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "ลบรูป", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    // Button to pick image
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "กดเพื่อแนบรูปภาพ (Photo Picker)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estimated Cost & Note
                OutlinedTextField(
                    value = estimatedCostStr,
                    onValueChange = { estimatedCostStr = it },
                    label = { Text("ยอดเงิน/ต้นทุนรวม (บาท - ไม่บังคับ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("หมายเหตุ (เช่น ซื้อจากตลาดสดปากช่อง)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                val isFormValid = title.isNotBlank() && quantityStr.toDoubleOrNull() != null && restockerName.isNotBlank()

                Button(
                    onClick = {
                        val qty = quantityStr.toDoubleOrNull() ?: 0.0
                        val cost = estimatedCostStr.toDoubleOrNull() ?: 0.0

                        val task = (initialTask ?: RestockTask(
                            title = title.trim(),
                            quantity = qty,
                            unit = unit.trim(),
                            restockerName = restockerName.trim(),
                            status = RestockStatus.PENDING
                        )).copy(
                            title = title.trim(),
                            quantity = qty,
                            unit = unit.trim(),
                            restockerName = restockerName.trim(),
                            imageUri = imageUriString,
                            estimatedCost = cost,
                            note = note.trim(),
                            rawMaterialId = selectedRawMaterialId
                        )

                        viewModel.saveRestockTask(task) {
                            onDismiss()
                        }
                    },
                    enabled = isFormValid && !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_restock_task_button")
                ) {
                    Text(
                        text = if (initialTask == null) "บันทึกงานที่ต้องเพิ่มสต็อก" else "บันทึกการแก้ไข",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Copies picked Uri to app internal storage for persistent offline access
 */
private suspend fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val fileName = "restock_img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
