package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class InventoryRepository(private val dao: InventoryDao) {

    val allRawMaterials: Flow<List<RawMaterial>> = dao.getAllRawMaterials()
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allRecipeItems: Flow<List<ProductRecipeItem>> = dao.getAllRecipeItems()
    val allMovements: Flow<List<StockMovement>> = dao.getAllMovements()
    val allRestockTasks: Flow<List<RestockTask>> = dao.getAllRestockTasks()

    /**
     * Combined flow providing Products with their full recipe details and stock feasibility
     */
    val productsWithRecipes: Flow<List<ProductWithRecipeDetails>> = combine(
        allProducts,
        allRecipeItems,
        allRawMaterials
    ) { products, recipes, materials ->
        val materialMap = materials.associateBy { it.id }
        products.map { product ->
            val productRecipes = recipes.filter { it.productId == product.id }
            val ingredientDetails = productRecipes.mapNotNull { recipeItem ->
                val mat = materialMap[recipeItem.rawMaterialId]
                if (mat != null) {
                    RecipeItemDetail(
                        recipeItemId = recipeItem.id,
                        rawMaterialId = mat.id,
                        rawMaterialName = mat.name,
                        quantityRequired = recipeItem.quantityRequired,
                        unit = mat.unit,
                        currentStock = mat.currentStock,
                        costPerUnit = mat.costPerUnit
                    )
                } else null
            }
            ProductWithRecipeDetails(
                product = product,
                ingredients = ingredientDetails
            )
        }
    }

    suspend fun insertRawMaterial(material: RawMaterial) = dao.insertRawMaterial(material)
    suspend fun updateRawMaterial(material: RawMaterial) = dao.updateRawMaterial(material)
    suspend fun deleteRawMaterial(material: RawMaterial) = dao.deleteRawMaterial(material)
    suspend fun updateRawMaterialStock(id: Long, newStock: Double) = dao.updateRawMaterialStock(id, newStock)

    suspend fun insertProduct(product: Product) = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)

    suspend fun saveProductRecipe(productId: Long, items: List<ProductRecipeItem>) {
        dao.replaceRecipeForProduct(productId, items)
    }

    suspend fun getRecipeItemsForProduct(productId: Long) = dao.getRecipeItemsForProductSync(productId)

    // Atomic stock actions
    suspend fun processSaleDeduction(productId: Long, quantity: Int, note: String) =
        dao.processSaleDeduction(productId, quantity, note)

    suspend fun processBatchProduction(productId: Long, quantity: Int, note: String) =
        dao.processBatchProduction(productId, quantity, note)

    suspend fun restockRawMaterial(id: Long, addedQuantity: Double, cost: Double, note: String) =
        dao.restockRawMaterial(id, addedQuantity, cost, note)

    suspend fun adjustRawMaterialStock(id: Long, newStock: Double, reason: String) =
        dao.adjustRawMaterialStock(id, newStock, reason)

    suspend fun addAndApplyRestockTask(task: RestockTask): Long =
        dao.completeRestockTaskAndApplyStock(task)

    suspend fun deleteRestockTask(task: RestockTask) =
        dao.deleteRestockTask(task)

    suspend fun clearMovements() = dao.clearAllMovements()
}
