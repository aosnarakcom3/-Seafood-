package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Raw Material entity (วัตถุดิบ)
 * e.g. ใบชา, นมสด, ไข่มุก, น้ำเชื่อม, แป้งสาลี, เนย, บรรจุภัณฑ์
 */
@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "ทั่วไป",
    val currentStock: Double,
    val unit: String, // กรัม (g), มล. (ml), ชิ้น (pcs), กก. (kg), ลิตร (L)
    val minStockThreshold: Double = 0.0, // เตือนเมื่อต่ำกว่าค่านี้
    val costPerUnit: Double = 0.0, // ต้นทุนต่อหน่วย (บาท)
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockThreshold && minStockThreshold > 0

    val isOutOfStock: Boolean
        get() = currentStock <= 0.0
}

/**
 * Finished Product entity (สินค้าสำเร็จรูป)
 * e.g. ชานมไข่มุก, มัทฉะลาเต้, ครัวซองต์
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "ทั่วไป",
    val price: Double = 0.0,
    val currentStock: Int = 0, // สำหรับสินค้าที่ผลิตสต็อกพร้อมขาย
    val trackFinishedStock: Boolean = false, // มีการนับสต็อกสำเร็จรูปหรือไม่
    val minStockThreshold: Int = 0, // เตือนเมื่อสต็อกสินค้าต่ำกว่าค่านี้
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = trackFinishedStock && currentStock <= minStockThreshold && minStockThreshold > 0

    val isOutOfStock: Boolean
        get() = trackFinishedStock && currentStock <= 0
}

/**
 * Bill of Materials (BOM) / Recipe Item (สูตรการผลิต / วัตถุดิบต่อ 1 หน่วยสินค้า)
 */
@Entity(
    tableName = "product_recipes",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RawMaterial::class,
            parentColumns = ["id"],
            childColumns = ["rawMaterialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"]), Index(value = ["rawMaterialId"])]
)
data class ProductRecipeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val rawMaterialId: Long,
    val quantityRequired: Double // ปริมาณวัตถุดิบที่ใช้ต่อสินค้า 1 ชิ้น
)

/**
 * Stock Movement Log (ประวัติการตัดสต็อก รับเข้า ปรับปรุง)
 */
@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MovementType,
    val referenceName: String, // e.g. ชื่อสินค้า หรือ ชื่อวัตถุดิบ
    val quantityDelta: Double, // ติดลบ = ตัดออก, บวก = รับเข้า
    val unit: String,
    val totalCostOrPrice: Double = 0.0,
    val note: String = ""
)

enum class MovementType(val title: String) {
    SALE_DEDUCTION("ตัดยอดจากการขาย"),
    PRODUCTION_DEDUCTION("ตัดวัตถุดิบเพื่อผลิต"),
    RESTOCK_RAW("รับเข้าวัตถุดิบ"),
    RESTOCK_PRODUCT("รับเข้าสินค้าสำเร็จรูป"),
    ADJUSTMENT("ปรับปรุงยอดสต็อก")
}

/**
 * Helper composite data structure for Recipe Details
 */
data class RecipeItemDetail(
    val recipeItemId: Long,
    val rawMaterialId: Long,
    val rawMaterialName: String,
    val quantityRequired: Double,
    val unit: String,
    val currentStock: Double,
    val costPerUnit: Double
) {
    val estimatedItemCost: Double
        get() = quantityRequired * costPerUnit
}

data class ProductWithRecipeDetails(
    val product: Product,
    val ingredients: List<RecipeItemDetail>
) {
    val totalCostPerUnit: Double
        get() = ingredients.sumOf { it.estimatedItemCost }

    val grossProfitMargin: Double
        get() = product.price - totalCostPerUnit

    // Check how many items can theoretically be made based on lowest available raw material
    val maxProducibleCount: Int
        get() {
            if (ingredients.isEmpty()) return 999
            val maxes = ingredients.map { item ->
                if (item.quantityRequired <= 0) 999
                else (item.currentStock / item.quantityRequired).toInt()
            }
            return (maxes.minOrNull() ?: 0).coerceAtLeast(0)
        }
}

/**
 * Restock Task Entity (งานที่ต้องเพิ่มสต็อก / บันทึกการเพิ่มสต็อกพร้อมแนบรูปภาพและระบุชื่อผู้เพิ่ม)
 */
@Entity(tableName = "restock_tasks")
data class RestockTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val itemType: String = "RAW_MATERIAL", // "RAW_MATERIAL" หรือ "PRODUCT"
    val itemId: Long = 0,
    val itemName: String,
    val category: String = "ทั่วไป",
    val quantityAdded: Double,
    val unit: String,
    val recordedBy: String, // ชื่อผู้เพิ่มสต็อก (ต้องระบุ เช่น สมชาย, แนน, บอย)
    val imageUri: String? = null, // รูปถ่ายสินค้า/ใบเสร็จ/หลักฐานการรับเข้า
    val supplierOrSource: String = "", // ร้านค้า หรือ ผู้จำหน่าย
    val totalCost: Double = 0.0, // ค่าใช้จ่ายรวม
    val note: String = "", // หมายเหตุ (เช่น เลขที่บิล, สภาพสินค้า)
    val status: String = "COMPLETED" // "COMPLETED" (เพิ่มเรียบร้อย) หรือ "PENDING" (งานที่ต้องสั่งเพิ่ม)
)
