package my.app.recipe.model.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import my.app.recipe.model.entities.Recipe

@Dao
interface RecipeDao {
    @Insert
    suspend fun insertRecipeDetails(recipe : Recipe)

    @Query("SELECT * FROM RECIPES_TABLE ORDER BY ID")
    fun getAllDishesList(): Flow<List<Recipe>>

    @Update
    suspend fun updateFavDishDetails(favDish: Recipe)


    @Query("SELECT * FROM RECIPES_TABLE WHERE favorite_dish = 1")
    fun getFavoriteDishesList(): Flow<List<Recipe>>

    @Delete
    suspend fun deleteFavDishDetails(favDish: Recipe)


    @Query("SELECT * FROM RECIPES_TABLE WHERE type = :filterType")
    fun getFilteredDishesList(filterType: String): Flow<List<Recipe>>

}


