package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- Raw Materials ---
    @Query("SELECT * FROM raw_materials ORDER BY name ASC")
    fun getAllRawMaterials(): Flow<List<RawMaterial>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getRawMaterialById(id: Long): RawMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterial(material: RawMaterial): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterials(materials: List<RawMaterial>)

    @Update
    suspend fun updateRawMaterial(material: RawMaterial)

    @Delete
    suspend fun deleteRawMaterial(material: RawMaterial)

    @Query("UPDATE raw_materials SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateRawMaterialStock(id: Long, newStock: Double, timestamp: Long = System.currentTimeMillis())

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateProductStock(id: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    // --- Recipes (BOM) ---
    @Query("SELECT * FROM product_recipes WHERE productId = :productId")
    fun getRecipeItemsForProduct(productId: Long): Flow<List<ProductRecipeItem>>

    @Query("SELECT * FROM product_recipes WHERE productId = :productId")
    suspend fun getRecipeItemsForProductSync(productId: Long): List<ProductRecipeItem>

    @Query("SELECT * FROM product_recipes")
    fun getAllRecipeItems(): Flow<List<ProductRecipeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItem(item: ProductRecipeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItems(items: List<ProductRecipeItem>)

    @Query("DELETE FROM product_recipes WHERE productId = :productId")
    suspend fun deleteRecipeItemsByProductId(productId: Long)

    @Transaction
    suspend fun replaceRecipeForProduct(productId: Long, items: List<ProductRecipeItem>) {
        deleteRecipeItemsByProductId(productId)
        insertRecipeItems(items)
    }

    // --- Stock Movements ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long

    @Query("DELETE FROM stock_movements")
    suspend fun clearAllMovements()

    // --- ATOMIC BOM DEDUCTION: SALE ---
    /**
     * Deducts raw materials according to the product's recipe for a sale.
     * Also updates finished stock if tracked, and logs transactions.
     */
    @Transaction
    suspend fun processSaleDeduction(
        productId: Long,
        quantity: Int,
        note: String
    ): Boolean {
        val product = getProductById(productId) ?: return false
        val recipeItems = getRecipeItemsForProductSync(productId)
        val now = System.currentTimeMillis()

        // 1. Deduct raw materials based on formula
        for (item in recipeItems) {
            val raw = getRawMaterialById(item.rawMaterialId) ?: continue
            val needed = item.quantityRequired * quantity
            val newStock = raw.currentStock - needed
            updateRawMaterialStock(raw.id, newStock, now)

            // Log individual raw material movement
            insertMovement(
                StockMovement(
                    timestamp = now,
                    type = MovementType.SALE_DEDUCTION,
                    referenceName = "${raw.name} (จากขาย ${product.name} $quantity ชิ้น)",
                    quantityDelta = -needed,
                    unit = raw.unit,
                    totalCostOrPrice = needed * raw.costPerUnit,
                    note = if (note.isNotBlank()) note else "ตัดสต็อกตามสูตรการขาย"
                )
            )
        }

        // 2. If finished stock is tracked, deduct finished goods or increment sales
        if (product.trackFinishedStock) {
            val newProductStock = (product.currentStock - quantity).coerceAtLeast(0)
            updateProductStock(product.id, newProductStock, now)
        }

        // 3. Log sale summary entry
        insertMovement(
            StockMovement(
                timestamp = now,
                type = MovementType.SALE_DEDUCTION,
                referenceName = product.name,
                quantityDelta = -quantity.toDouble(),
                unit = "ชิ้น",
                totalCostOrPrice = product.price * quantity,
                note = "ขายสินค้าสำเร็จรูป: $quantity ชิ้น" + if (note.isNotBlank()) " ($note)" else ""
            )
        )

        return true
    }

    // --- ATOMIC BOM DEDUCTION: PRODUCTION ---
    /**
     * Deducts raw materials and ADDS to finished product stock (e.g. baking 20 croissants)
     */
    @Transaction
    suspend fun processBatchProduction(
        productId: Long,
        quantity: Int,
        note: String
    ): Boolean {
        val product = getProductById(productId) ?: return false
        val recipeItems = getRecipeItemsForProductSync(productId)
        val now = System.currentTimeMillis()

        // 1. Deduct raw materials
        for (item in recipeItems) {
            val raw = getRawMaterialById(item.rawMaterialId) ?: continue
            val needed = item.quantityRequired * quantity
            val newStock = raw.currentStock - needed
            updateRawMaterialStock(raw.id, newStock, now)

            insertMovement(
                StockMovement(
                    timestamp = now,
                    type = MovementType.PRODUCTION_DEDUCTION,
                    referenceName = "${raw.name} (ใช้ผลิต ${product.name} $quantity ชิ้น)",
                    quantityDelta = -needed,
                    unit = raw.unit,
                    totalCostOrPrice = needed * raw.costPerUnit,
                    note = "ตัดยอดวัตถุดิบเข้ากระบวนการผลิต"
                )
            )
        }

        // 2. Add to finished product stock
        val newProductStock = product.currentStock + quantity
        updateProductStock(product.id, newProductStock, now)

        // 3. Log production movement
        insertMovement(
            StockMovement(
                timestamp = now,
                type = MovementType.RESTOCK_PRODUCT,
                referenceName = product.name,
                quantityDelta = quantity.toDouble(),
                unit = "ชิ้น",
                totalCostOrPrice = product.price * quantity,
                note = "ผลิตเสร็จนำเข้าสต็อก $quantity ชิ้น" + if (note.isNotBlank()) " ($note)" else ""
            )
        )

        return true
    }

    // --- RESTOCK RAW MATERIAL ---
    @Transaction
    suspend fun restockRawMaterial(
        rawMaterialId: Long,
        addedQuantity: Double,
        cost: Double,
        note: String
    ): Boolean {
        val raw = getRawMaterialById(rawMaterialId) ?: return false
        val now = System.currentTimeMillis()
        val newStock = raw.currentStock + addedQuantity
        updateRawMaterialStock(raw.id, newStock, now)

        insertMovement(
            StockMovement(
                timestamp = now,
                type = MovementType.RESTOCK_RAW,
                referenceName = raw.name,
                quantityDelta = addedQuantity,
                unit = raw.unit,
                totalCostOrPrice = cost,
                note = if (note.isNotBlank()) note else "รับเข้าวัตถุดิบ"
            )
        )
        return true
    }

    // --- MANUAL ADJUSTMENT ---
    @Transaction
    suspend fun adjustRawMaterialStock(
        rawMaterialId: Long,
        newStock: Double,
        reason: String
    ): Boolean {
        val raw = getRawMaterialById(rawMaterialId) ?: return false
        val now = System.currentTimeMillis()
        val delta = newStock - raw.currentStock
        updateRawMaterialStock(raw.id, newStock, now)

        insertMovement(
            StockMovement(
                timestamp = now,
                type = MovementType.ADJUSTMENT,
                referenceName = raw.name,
                quantityDelta = delta,
                unit = raw.unit,
                totalCostOrPrice = 0.0,
                note = "ปรับปรุงสต็อก: $reason (เดิม ${raw.currentStock} -> ใหม่ $newStock)"
            )
        )
        return true
    }

    // --- Restock Tasks (งานที่ต้องเพิ่มสต็อก / บันทึกการเพิ่มสต็อกพร้อมแนบรูปภาพและระบุชื่อผู้เพิ่ม) ---
    @Query("SELECT * FROM restock_tasks ORDER BY timestamp DESC")
    fun getAllRestockTasks(): Flow<List<RestockTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestockTask(task: RestockTask): Long

    @Delete
    suspend fun deleteRestockTask(task: RestockTask)

    @Transaction
    suspend fun completeRestockTaskAndApplyStock(task: RestockTask): Long {
        val taskId = insertRestockTask(task)
        val now = System.currentTimeMillis()

        if (task.itemType == "RAW_MATERIAL") {
            val raw = getRawMaterialById(task.itemId)
            if (raw != null) {
                val newStock = raw.currentStock + task.quantityAdded
                updateRawMaterialStock(raw.id, newStock, now)
                insertMovement(
                    StockMovement(
                        timestamp = now,
                        type = MovementType.RESTOCK_RAW,
                        referenceName = raw.name,
                        quantityDelta = task.quantityAdded,
                        unit = task.unit,
                        totalCostOrPrice = task.totalCost,
                        note = "ผู้เพิ่ม: ${task.recordedBy} | ${task.note}"
                    )
                )
            }
        } else if (task.itemType == "PRODUCT") {
            val prod = getProductById(task.itemId)
            if (prod != null) {
                val newStock = prod.currentStock + task.quantityAdded.toInt()
                updateProductStock(prod.id, newStock, now)
                insertMovement(
                    StockMovement(
                        timestamp = now,
                        type = MovementType.RESTOCK_PRODUCT,
                        referenceName = prod.name,
                        quantityDelta = task.quantityAdded,
                        unit = task.unit,
                        totalCostOrPrice = task.totalCost,
                        note = "ผู้เพิ่ม: ${task.recordedBy} | ${task.note}"
                    )
                )
            }
        }
        return taskId
    }
}
