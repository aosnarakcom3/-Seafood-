package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.data.ProductWithRecipeDetails
import com.example.data.RawMaterial
import com.example.ui.dialogs.AddEditRawMaterialDialog
import com.example.ui.dialogs.BarcodeScannerDialog
import com.example.ui.dialogs.BatchProductionDialog
import com.example.ui.dialogs.DeductStockDialog
import com.example.ui.dialogs.ProductRecipeDialog
import com.example.ui.dialogs.RestockMaterialDialog
import com.example.ui.dialogs.ScannedResultActionDialog
import com.example.ui.dialogs.StockAlertsDialog
import com.example.ui.screens.MovementsHistoryScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.QuickDeductScreen
import com.example.ui.screens.RawMaterialsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RestockTasksScreen
import com.example.ui.screens.StockAlertsScreen

enum class AppTab(val title: String, val icon: ImageVector) {
    DEDUCT("ตัดสต็อก", Icons.Default.PointOfSale),
    RAW_MATERIALS("วัตถุดิบ", Icons.Default.Inventory2),
    ALERTS("เตือนสต็อก", Icons.Default.NotificationsActive),
    RESTOCK_TASKS("งานเพิ่มสต็อก", Icons.Default.Assignment),
    PRODUCTS("สินค้า & สูตร", Icons.Default.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: InventoryViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.DEDUCT) }
    val snackbarHostState = remember { SnackbarHostState() }

    val alertCount by viewModel.totalAlertCount.collectAsStateWithLifecycle()
    val allRawMaterials by viewModel.allRawMaterials.collectAsStateWithLifecycle()
    val productsWithRecipes by viewModel.productsWithRecipes.collectAsStateWithLifecycle()

    // Dialog states
    var showScanner by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // Sub-dialogs opened from scan or action
    var deductTargetProduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }
    var produceTargetProduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }
    var editTargetProduct by remember { mutableStateOf<ProductWithRecipeDetails?>(null) }
    var restockTargetMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var editTargetMaterial by remember { mutableStateOf<RawMaterial?>(null) }

    // Dialog for creating product or raw material with prefilled scanned barcode
    var newProductWithBarcode by remember { mutableStateOf<String?>(null) }
    var newMaterialWithBarcode by remember { mutableStateOf<String?>(null) }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Collect UI messages
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Stock อุดมSesfoodปากช่อง",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ตัดยอดวัตถุดิบ & สต็อกสินค้า",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // History Icon Action
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("top_app_bar_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "ประวัติสต็อก",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Barcode / QR Scanner Quick Action
                    IconButton(
                        onClick = { showScanner = true },
                        modifier = Modifier.testTag("top_app_bar_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "สแกนบาร์โค้ด",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Stock Alerts Quick Switch with Badge
                    IconButton(
                        onClick = { selectedTab = AppTab.ALERTS },
                        modifier = Modifier.testTag("top_app_bar_alerts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text("$alertCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (alertCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "การแจ้งเตือนสต็อก",
                                tint = if (alertCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == AppTab.ALERTS && alertCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text("$alertCount")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.DEDUCT -> QuickDeductScreen(
                    viewModel = viewModel,
                    onNavigateToRawMaterials = { selectedTab = AppTab.RAW_MATERIALS },
                    onNavigateToAlerts = { selectedTab = AppTab.ALERTS }
                )
                AppTab.RAW_MATERIALS -> RawMaterialsScreen(viewModel = viewModel)
                AppTab.ALERTS -> StockAlertsScreen(
                    viewModel = viewModel,
                    onRestockMaterial = { mat -> restockTargetMaterial = mat },
                    onEditMaterial = { mat -> editTargetMaterial = mat },
                    onProduceProduct = { prod ->
                        val targetWithRecipe = productsWithRecipes.firstOrNull { it.product.id == prod.id }
                        if (targetWithRecipe != null) {
                            produceTargetProduct = targetWithRecipe
                        }
                    }
                )
                AppTab.RESTOCK_TASKS -> RestockTasksScreen(viewModel = viewModel)
                AppTab.PRODUCTS -> ProductsScreen(viewModel = viewModel)
            }
        }
    }

    // --- Active Dialogs ---

    // 1. History Modal Dialog
    if (showHistoryDialog) {
        Dialog(
            onDismissRequest = { showHistoryDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxSize(0.9f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ประวัติการเคลื่อนไหวสต็อก",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showHistoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "ปิด")
                        }
                    }
                    MovementsHistoryScreen(
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 2. Barcode Scanner Viewfinder
    if (showScanner) {
        BarcodeScannerDialog(
            onCodeScanned = { code ->
                showScanner = false
                scannedCode = code
            },
            onDismiss = { showScanner = false }
        )
    }

    // 3. Scanned Result Action Modal
    scannedCode?.let { code ->
        val matchedProduct = viewModel.findProductByBarcode(code)
        val matchedMaterial = viewModel.findMaterialByBarcode(code)

        ScannedResultActionDialog(
            scannedCode = code,
            matchedProduct = matchedProduct,
            matchedMaterial = matchedMaterial,
            viewModel = viewModel,
            onDeductProduct = { deductTargetProduct = it },
            onProduceProduct = { produceTargetProduct = it },
            onEditProduct = { editTargetProduct = it },
            onAddProductWithCode = { newProductWithBarcode = it },
            onAddMaterialWithCode = { newMaterialWithBarcode = it },
            onDismiss = { scannedCode = null }
        )
    }

    // 4. Deduct Stock Dialog
    deductTargetProduct?.let { p ->
        DeductStockDialog(
            productWithRecipe = p,
            viewModel = viewModel,
            onDismiss = { deductTargetProduct = null }
        )
    }

    // 5. Batch Production Dialog
    produceTargetProduct?.let { p ->
        BatchProductionDialog(
            productWithRecipe = p,
            viewModel = viewModel,
            onDismiss = { produceTargetProduct = null }
        )
    }

    // 6. Product Recipe Dialog
    editTargetProduct?.let { p ->
        ProductRecipeDialog(
            initialProduct = p,
            allRawMaterials = allRawMaterials,
            viewModel = viewModel,
            onDismiss = { editTargetProduct = null }
        )
    }

    // 7. Restock Material Dialog
    restockTargetMaterial?.let { m ->
        RestockMaterialDialog(
            material = m,
            viewModel = viewModel,
            onDismiss = { restockTargetMaterial = null }
        )
    }

    // 8. Edit Material Dialog
    editTargetMaterial?.let { m ->
        AddEditRawMaterialDialog(
            initialMaterial = m,
            viewModel = viewModel,
            onDismiss = { editTargetMaterial = null }
        )
    }

    // 9. Add Product with prefilled scanned barcode
    newProductWithBarcode?.let { barcode ->
        ProductRecipeDialog(
            initialProduct = ProductWithRecipeDetails(
                product = Product(name = "", sku = barcode, barcode = barcode),
                ingredients = emptyList()
            ),
            allRawMaterials = allRawMaterials,
            viewModel = viewModel,
            onDismiss = { newProductWithBarcode = null }
        )
    }

    // 10. Add Raw Material with prefilled scanned barcode
    newMaterialWithBarcode?.let { barcode ->
        AddEditRawMaterialDialog(
            initialMaterial = RawMaterial(name = "", sku = barcode, barcode = barcode, currentStock = 0.0, unit = "กรัม"),
            viewModel = viewModel,
            onDismiss = { newMaterialWithBarcode = null }
        )
    }
}
