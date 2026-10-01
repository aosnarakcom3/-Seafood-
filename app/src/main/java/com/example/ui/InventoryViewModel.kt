package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.InventoryRepository
import com.example.data.MovementType
import com.example.data.Product
import com.example.data.ProductRecipeItem
import com.example.data.ProductWithRecipeDetails
import com.example.data.RawMaterial
import com.example.data.RestockTask
import com.example.data.StockMovement
import com.example.util.StockAlertNotificationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MaterialDeductionPreview(
    val rawMaterialId: Long,
    val name: String,
    val unit: String,
    val requiredPerUnit: Double,
    val totalRequired: Double,
    val currentStock: Double,
    val remainingStockAfter: Double,
    val isShortage: Boolean
)

data class DeductionValidationResult(
    val canDeduct: Boolean,
    val shortages: List<MaterialDeductionPreview>,
    val previews: List<MaterialDeductionPreview>
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = InventoryRepository(db.inventoryDao())
        StockAlertNotificationManager.initNotificationChannel(application)
    }

    // UI Message Events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Search and Filters
    val rawMaterialSearchQuery = MutableStateFlow("")
    val rawMaterialSelectedCategory = MutableStateFlow("ทั้งหมด")

    val productSearchQuery = MutableStateFlow("")
    val productSelectedCategory = MutableStateFlow("ทั้งหมด")

    // Raw Materials Data
    val allRawMaterials: StateFlow<List<RawMaterial>> = repository.allRawMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRawMaterials: StateFlow<List<RawMaterial>> = combine(
        allRawMaterials,
        rawMaterialSearchQuery,
        rawMaterialSelectedCategory
    ) { materials, query, category ->
        materials.filter { mat ->
            val matchQuery = query.isBlank() ||
                    mat.name.contains(query, ignoreCase = true) ||
                    mat.sku.contains(query, ignoreCase = true) ||
                    mat.barcode.contains(query, ignoreCase = true)
            val matchCat = category == "ทั้งหมด" || mat.category == category
            matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMaterials: StateFlow<List<RawMaterial>> = allRawMaterials
        .map { list -> list.filter { it.isLowStock || it.isOutOfStock } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawMaterialCategories: StateFlow<List<String>> = allRawMaterials
        .map { list ->
            listOf("ทั้งหมด") + list.map { it.category.ifBlank { "ทั่วไป" } }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ทั้งหมด"))

    // Products Data with Recipes
    val productsWithRecipes: StateFlow<List<ProductWithRecipeDetails>> = repository.productsWithRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<ProductWithRecipeDetails>> = combine(
        productsWithRecipes,
        productSearchQuery,
        productSelectedCategory
    ) { products, query, category ->
        products.filter { p ->
            val matchQuery = query.isBlank() ||
                    p.product.name.contains(query, ignoreCase = true) ||
                    p.product.sku.contains(query, ignoreCase = true) ||
                    p.product.barcode.contains(query, ignoreCase = true)
            val matchCat = category == "ทั้งหมด" || p.product.category == category
            matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = productsWithRecipes
        .map { list -> list.map { it.product }.filter { it.isLowStock || it.isOutOfStock } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAlertCount: StateFlow<Int> = combine(lowStockMaterials, lowStockProducts) { mats, prods ->
        mats.size + prods.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productCategories: StateFlow<List<String>> = productsWithRecipes
        .map { list ->
            listOf("ทั้งหมด") + list.map { it.product.category.ifBlank { "ทั่วไป" } }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ทั้งหมด"))

    // Stock Movement History
    val movements: StateFlow<List<StockMovement>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock Movement Filter
    val movementFilter = MutableStateFlow<MovementType?>(null)
    val filteredMovements: StateFlow<List<StockMovement>> = combine(
        movements,
        movementFilter
    ) { list, filter ->
        if (filter == null) list else list.filter { it.type == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Barcode / QR Code Lookup ---
    fun findProductByBarcode(code: String): ProductWithRecipeDetails? {
        val clean = code.trim()
        return productsWithRecipes.value.firstOrNull {
            it.product.barcode.equals(clean, ignoreCase = true) ||
                    it.product.sku.equals(clean, ignoreCase = true) ||
                    it.product.name.equals(clean, ignoreCase = true)
        }
    }

    fun findMaterialByBarcode(code: String): RawMaterial? {
        val clean = code.trim()
        return allRawMaterials.value.firstOrNull {
            it.barcode.equals(clean, ignoreCase = true) ||
                    it.sku.equals(clean, ignoreCase = true) ||
                    it.name.equals(clean, ignoreCase = true)
        }
    }

    // --- Deduction Calculator Helper ---
    fun calculateDeductionPreview(
        productWithRecipe: ProductWithRecipeDetails,
        quantity: Int
    ): DeductionValidationResult {
        val previews = productWithRecipe.ingredients.map { ingredient ->
            val totalNeeded = ingredient.quantityRequired * quantity
            val remaining = ingredient.currentStock - totalNeeded
            MaterialDeductionPreview(
                rawMaterialId = ingredient.rawMaterialId,
                name = ingredient.rawMaterialName,
                unit = ingredient.unit,
                requiredPerUnit = ingredient.quantityRequired,
                totalRequired = totalNeeded,
                currentStock = ingredient.currentStock,
                remainingStockAfter = remaining,
                isShortage = remaining < 0
            )
        }
        val shortages = previews.filter { it.isShortage }
        return DeductionValidationResult(
            canDeduct = shortages.isEmpty(),
            shortages = shortages,
            previews = previews
        )
    }

    // --- ACTIONS ---

    /**
     * Deduct raw materials for Product Sale
     */
    fun processSale(
        productWithRecipe: ProductWithRecipeDetails,
        quantity: Int,
        note: String,
        forceAllowDeficit: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (quantity <= 0) {
                _userMessage.emit("กรุณาระบุจำนวนที่มากกว่า 0")
                return@launch
            }

            val validation = calculateDeductionPreview(productWithRecipe, quantity)
            if (!validation.canDeduct && !forceAllowDeficit) {
                val names = validation.shortages.joinToString(", ") { it.name }
                _userMessage.emit("วัตถุดิบไม่พอ: $names")
                return@launch
            }

            val success = repository.processSaleDeduction(
                productId = productWithRecipe.product.id,
                quantity = quantity,
                note = note
            )
            if (success) {
                _userMessage.emit("ตัดยอดวัตถุดิบสำเร็จ! (${productWithRecipe.product.name} $quantity ชิ้น)")
                checkAndSendAlerts()
                onSuccess()
            } else {
                _userMessage.emit("เกิดข้อผิดพลาดในการตัดยอด")
            }
        }
    }

    /**
     * Batch Production (ตัดวัตถุดิบ และเพิ่มสต็อกสินค้าสำเร็จรูป)
     */
    fun processProduction(
        productWithRecipe: ProductWithRecipeDetails,
        quantity: Int,
        note: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (quantity <= 0) {
                _userMessage.emit("กรุณาระบุจำนวนการผลิตที่มากกว่า 0")
                return@launch
            }

            val validation = calculateDeductionPreview(productWithRecipe, quantity)
            if (!validation.canDeduct) {
                val names = validation.shortages.joinToString(", ") { it.name }
                _userMessage.emit("วัตถุดิบไม่พอสำหรับการผลิต: $names")
                return@launch
            }

            val success = repository.processBatchProduction(
                productId = productWithRecipe.product.id,
                quantity = quantity,
                note = note
            )
            if (success) {
                _userMessage.emit("บันทึกการผลิตสำเร็จ! เพิ่มสต็อก ${productWithRecipe.product.name} $quantity ชิ้น")
                checkAndSendAlerts()
                onSuccess()
            } else {
                _userMessage.emit("เกิดข้อผิดพลาดในการบันทึกการผลิต")
            }
        }
    }

    /**
     * Restock raw material
     */
    fun restockMaterial(
        rawMaterialId: Long,
        addedQuantity: Double,
        cost: Double,
        note: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (addedQuantity <= 0) {
                _userMessage.emit("กรุณาระบุจำนวนรับเข้าที่ถูกต้อง")
                return@launch
            }
            val ok = repository.restockRawMaterial(rawMaterialId, addedQuantity, cost, note)
            if (ok) {
                _userMessage.emit("รับเข้าวัตถุดิบเรียบร้อยแล้ว")
                onSuccess()
            }
        }
    }

    /**
     * Adjust raw material stock directly
     */
    fun adjustMaterialStock(
        rawMaterialId: Long,
        newStock: Double,
        reason: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val ok = repository.adjustRawMaterialStock(rawMaterialId, newStock, reason)
            if (ok) {
                _userMessage.emit("ปรับปรุงยอดสต็อกเรียบร้อยแล้ว")
                checkAndSendAlerts()
                onSuccess()
            }
        }
    }

    /**
     * Save (Add or Update) Raw Material
     */
    fun saveRawMaterial(material: RawMaterial, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (material.name.isBlank()) {
                _userMessage.emit("กรุณากรอกชื่อวัตถุดิบ")
                return@launch
            }
            if (material.id == 0L) {
                repository.insertRawMaterial(material)
                _userMessage.emit("เพิ่มวัตถุดิบ '${material.name}' เรียบร้อย")
            } else {
                repository.updateRawMaterial(material)
                _userMessage.emit("อัปเดตข้อมูลวัตถุดิบ '${material.name}' แล้ว")
            }
            if (material.isLowStock || material.isOutOfStock) {
                StockAlertNotificationManager.sendRawMaterialLowStockAlert(getApplication(), material)
            }
            onSuccess()
        }
    }

    /**
     * Delete Raw Material
     */
    fun deleteRawMaterial(material: RawMaterial) {
        viewModelScope.launch {
            repository.deleteRawMaterial(material)
            _userMessage.emit("ลบวัตถุดิบ '${material.name}' เรียบร้อย")
        }
    }

    /**
     * Save Product & its Recipe
     */
    fun saveProductWithRecipe(
        product: Product,
        recipeQuantities: Map<Long, Double>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (product.name.isBlank()) {
                _userMessage.emit("กรุณาระบุชื่อสินค้า")
                return@launch
            }
            val targetProductId = if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
                product.id
            }

            val recipeItems = recipeQuantities
                .filter { it.value > 0 }
                .map { (matId, qty) ->
                    ProductRecipeItem(
                        productId = targetProductId,
                        rawMaterialId = matId,
                        quantityRequired = qty
                    )
                }
            repository.saveProductRecipe(targetProductId, recipeItems)

            if (product.isLowStock || product.isOutOfStock) {
                StockAlertNotificationManager.sendProductLowStockAlert(getApplication(), product)
            }

            _userMessage.emit("บันทึกสินค้าและสูตรวัตถุดิบเรียบร้อย")
            onSuccess()
        }
    }

    /**
     * Delete Product
     */
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.emit("ลบสินค้า '${product.name}' เรียบร้อย")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearMovements()
            _userMessage.emit("ล้างประวัติการเคลื่อนไหวแล้ว")
        }
    }

    // --- Restock Tasks (งานที่ต้องเพิ่มสต็อก / บันทึกการเพิ่มสต็อกพร้อมแนบรูปภาพและระบุชื่อผู้เพิ่ม) ---
    val allRestockTasks: StateFlow<List<RestockTask>> = repository.allRestockTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitRestockTask(
        task: RestockTask,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addAndApplyRestockTask(task)
            _userMessage.emit("บันทึกงานเพิ่มสต็อก '${task.itemName}' เรียบร้อยแล้ว (โดย ${task.recordedBy})")
            onSuccess()
        }
    }

    fun deleteRestockTask(task: RestockTask) {
        viewModelScope.launch {
            repository.deleteRestockTask(task)
            _userMessage.emit("ลบรายการงานเพิ่มสต็อกเรียบร้อยแล้ว")
        }
    }

    private fun checkAndSendAlerts() {
        viewModelScope.launch {
            val materials = allRawMaterials.firstOrNull() ?: emptyList()
            for (m in materials) {
                if (m.isLowStock || m.isOutOfStock) {
                    StockAlertNotificationManager.sendRawMaterialLowStockAlert(getApplication(), m)
                }
            }

            val products = productsWithRecipes.firstOrNull() ?: emptyList()
            for (p in products) {
                if (p.product.isLowStock || p.product.isOutOfStock) {
                    StockAlertNotificationManager.sendProductLowStockAlert(getApplication(), p.product)
                }
            }
        }
    }
}
