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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.RestockTask
import com.example.ui.InventoryViewModel
import com.example.ui.components.numberFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun CompleteRestockTaskDialog(
    task: RestockTask,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var restockerName by remember { mutableStateOf(task.restockerName) }
    var applyToStock by remember { mutableStateOf(task.rawMaterialId != null) }
    var imageUriString by remember { mutableStateOf(task.imageUri) }

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ยืนยันการเพิ่มสต็อกเสร็จสิ้น",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item info
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (task.note.isNotBlank()) {
                                Text(
                                    text = task.note,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "+${numberFormatter.format(task.quantity)} ${task.unit}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Restocker Name (ระบุชื่อผู้เพิ่มสต็อก)
                OutlinedTextField(
                    value = restockerName,
                    onValueChange = { restockerName = it },
                    label = { Text("ระบุชื่อผู้เพิ่มสต็อก *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_restocker_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo attachment (แนบรูปภาพใบเสร็จ/สินค้า)
                Text(
                    text = "แนบรูปภาพ (หลักฐาน/ใบเสร็จ/สินค้า):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (imageUriString != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = File(imageUriString!!),
                            contentDescription = "รูปภาพแนบ",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { imageUriString = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "ลบรูป", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("แนบรูปถ่ายการเพิ่มสต็อก", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (task.rawMaterialId != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { applyToStock = !applyToStock },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = applyToStock,
                            onCheckedChange = { applyToStock = it }
                        )
                        Text(
                            text = "อัปเดตจำนวนเข้าคลังสต็อกวัตถุดิบจริงทันที (+${task.quantity} ${task.unit})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updatedTask = task.copy(
                            imageUri = imageUriString,
                            restockerName = restockerName.ifBlank { "ไม่ระบุชื่อ" }
                        )
                        viewModel.saveRestockTask(updatedTask) {
                            viewModel.completeRestockTask(
                                taskId = task.id,
                                restockerName = restockerName.ifBlank { "ผู้เพิ่มสต็อก" },
                                applyToStock = applyToStock,
                                onSuccess = onDismiss
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_complete_restock_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ยืนยันเพิ่มสต็อกสำเร็จ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
