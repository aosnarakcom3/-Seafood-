package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RawMaterial::class,
        Product::class,
        ProductRecipeItem::class,
        StockMovement::class,
        RestockTask::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stock_bom_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.inventoryDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: InventoryDao) {
            // 1. Seed Raw Materials
            val teaLeaf = RawMaterial(
                name = "ใบชาอัสสัมพรีเมียม",
                sku = "RM-TEA-01",
                category = "ชาและกาแฟ",
                currentStock = 2500.0,
                unit = "กรัม",
                minStockThreshold = 500.0,
                costPerUnit = 0.45
            )
            val matchaPowder = RawMaterial(
                name = "ผงมัทฉะอุจิแท้ 100%",
                sku = "RM-MATCHA-02",
                category = "ชาและกาแฟ",
                currentStock = 800.0,
                unit = "กรัม",
                minStockThreshold = 200.0,
                costPerUnit = 1.20
            )
            val freshMilk = RawMaterial(
                name = "นมสดพาสเจอร์ไรส์ เมจิ",
                sku = "RM-MILK-01",
                category = "นมและเนย",
                currentStock = 8500.0,
                unit = "มล.",
                minStockThreshold = 2000.0,
                costPerUnit = 0.06
            )
            val bobaPearls = RawMaterial(
                name = "ไข่มุกดำไต้หวันต้มสุก",
                sku = "RM-BOBA-01",
                category = "ท็อปปิ้ง",
                currentStock = 3200.0,
                unit = "กรัม",
                minStockThreshold = 600.0,
                costPerUnit = 0.12
            )
            val brownSugarSyrup = RawMaterial(
                name = "น้ำเชื่อมบราวน์ชูการ์เข้มข้น",
                sku = "RM-SYRUP-01",
                category = "สารให้ความหวาน",
                currentStock = 1800.0,
                unit = "มล.",
                minStockThreshold = 400.0,
                costPerUnit = 0.15
            )
            val cupAndLid = RawMaterial(
                name = "แก้ว 16oz + ฝาโดม + หลอด",
                sku = "RM-PKG-01",
                category = "บรรจุภัณฑ์",
                currentStock = 250.0,
                unit = "ชุด",
                minStockThreshold = 50.0,
                costPerUnit = 3.50
            )
            val breadFlour = RawMaterial(
                name = "แป้งสาลีทำขนมปัง (T55)",
                sku = "RM-FLR-01",
                category = "เบเกอรี่",
                currentStock = 4500.0,
                unit = "กรัม",
                minStockThreshold = 1000.0,
                costPerUnit = 0.08
            )
            val butter = RawMaterial(
                name = "เนยสดแท้ชนิดจืด (Pure Butter)",
                sku = "RM-BTR-01",
                category = "นมและเนย",
                currentStock = 2000.0,
                unit = "กรัม",
                minStockThreshold = 500.0,
                costPerUnit = 0.50
            )

            val rawIds = listOf(
                dao.insertRawMaterial(teaLeaf),
                dao.insertRawMaterial(matchaPowder),
                dao.insertRawMaterial(freshMilk),
                dao.insertRawMaterial(bobaPearls),
                dao.insertRawMaterial(brownSugarSyrup),
                dao.insertRawMaterial(cupAndLid),
                dao.insertRawMaterial(breadFlour),
                dao.insertRawMaterial(butter)
            )

            val idTea = rawIds[0]
            val idMatcha = rawIds[1]
            val idMilk = rawIds[2]
            val idBoba = rawIds[3]
            val idSyrup = rawIds[4]
            val idCup = rawIds[5]
            val idFlour = rawIds[6]
            val idButter = rawIds[7]

            // 2. Seed Products
            val prod1Id = dao.insertProduct(
                Product(
                    name = "ชานมไข่มุกไต้หวันพรีเมียม",
                    sku = "PRD-BOBA-01",
                    barcode = "PRD-BOBA-01",
                    category = "เครื่องดื่ม",
                    price = 55.0,
                    currentStock = 0,
                    trackFinishedStock = false
                )
            )
            val prod2Id = dao.insertProduct(
                Product(
                    name = "มัทฉะลาเต้เย็นสูตรเข้มข้น",
                    sku = "PRD-MATCHA-02",
                    barcode = "PRD-MATCHA-02",
                    category = "เครื่องดื่ม",
                    price = 70.0,
                    currentStock = 0,
                    trackFinishedStock = false
                )
            )
            val prod3Id = dao.insertProduct(
                Product(
                    name = "นมสดบราวน์ชูการ์ไข่มุก",
                    sku = "PRD-BRNSUG-03",
                    barcode = "PRD-BRNSUG-03",
                    category = "เครื่องดื่ม",
                    price = 60.0,
                    currentStock = 0,
                    trackFinishedStock = false
                )
            )
            val prod4Id = dao.insertProduct(
                Product(
                    name = "ครัวซองต์เนยสดฝรั่งเศส",
                    sku = "PRD-CRSN-04",
                    barcode = "PRD-CRSN-04",
                    category = "เบเกอรี่",
                    price = 65.0,
                    currentStock = 12,
                    trackFinishedStock = true,
                    minStockThreshold = 5
                )
            )

            // 3. Seed Recipe BOM (สูตรตัดยอดวัตถุดิบต่อ 1 ชิ้น)
            // ชานมไข่มุก: ใบชา 15g, นมสด 120ml, ไข่มุก 50g, แก้ว 1ชุด
            dao.insertRecipeItems(
                listOf(
                    ProductRecipeItem(productId = prod1Id, rawMaterialId = idTea, quantityRequired = 15.0),
                    ProductRecipeItem(productId = prod1Id, rawMaterialId = idMilk, quantityRequired = 120.0),
                    ProductRecipeItem(productId = prod1Id, rawMaterialId = idBoba, quantityRequired = 50.0),
                    ProductRecipeItem(productId = prod1Id, rawMaterialId = idCup, quantityRequired = 1.0)
                )
            )

            // มัทฉะลาเต้: ผงมัทฉะ 10g, นมสด 150ml, น้ำเชื่อม 15ml, แก้ว 1ชุด
            dao.insertRecipeItems(
                listOf(
                    ProductRecipeItem(productId = prod2Id, rawMaterialId = idMatcha, quantityRequired = 10.0),
                    ProductRecipeItem(productId = prod2Id, rawMaterialId = idMilk, quantityRequired = 150.0),
                    ProductRecipeItem(productId = prod2Id, rawMaterialId = idSyrup, quantityRequired = 15.0),
                    ProductRecipeItem(productId = prod2Id, rawMaterialId = idCup, quantityRequired = 1.0)
                )
            )

            // นมสดบราวน์ชูการ์: นมสด 180ml, น้ำเชื่อม 30ml, ไข่มุก 50g, แก้ว 1ชุด
            dao.insertRecipeItems(
                listOf(
                    ProductRecipeItem(productId = prod3Id, rawMaterialId = idMilk, quantityRequired = 180.0),
                    ProductRecipeItem(productId = prod3Id, rawMaterialId = idSyrup, quantityRequired = 30.0),
                    ProductRecipeItem(productId = prod3Id, rawMaterialId = idBoba, quantityRequired = 50.0),
                    ProductRecipeItem(productId = prod3Id, rawMaterialId = idCup, quantityRequired = 1.0)
                )
            )

            // ครัวซองต์: แป้ง 80g, เนย 50g
            dao.insertRecipeItems(
                listOf(
                    ProductRecipeItem(productId = prod4Id, rawMaterialId = idFlour, quantityRequired = 80.0),
                    ProductRecipeItem(productId = prod4Id, rawMaterialId = idButter, quantityRequired = 50.0)
                )
            )

            // 4. Initial Movement Log
            dao.insertMovement(
                StockMovement(
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    type = MovementType.RESTOCK_RAW,
                    referenceName = "ตั้งต้นสต็อกระบบ",
                    quantityDelta = 1.0,
                    unit = "ชุด",
                    totalCostOrPrice = 0.0,
                    note = "สร้างฐานข้อมูลตัวอย่างพร้อมสูตรตัดสต็อก BOM"
                )
            )
        }
    }
}
