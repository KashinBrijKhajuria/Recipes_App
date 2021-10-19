package my.app.recipe.model.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import my.app.recipe.model.entities.Recipe

class RecipeRepository( private val recipeDao: RecipeDao) {

    @WorkerThread
    suspend fun insertRecipeData(recipe: Recipe) {
        recipeDao.insertRecipeDetails(recipe)
    }

    val allDishesList: Flow<List<Recipe>> = recipeDao.getAllDishesList()

    @WorkerThread
    suspend fun updateFavDishData(favDish: Recipe) {
        recipeDao.updateFavDishDetails(favDish)
    }

    val favoriteDishes: Flow<List<Recipe>> = recipeDao.getFavoriteDishesList()
    suspend fun deleteFavDishData(favDish: Recipe) {
        recipeDao.deleteFavDishDetails(favDish)
    }


    fun filteredListDishes(value: String): Flow<List<Recipe>> =
        recipeDao.getFilteredDishesList(value)
}



