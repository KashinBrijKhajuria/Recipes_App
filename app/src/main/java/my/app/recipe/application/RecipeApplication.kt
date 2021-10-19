package my.app.recipe.application

import android.app.Application
import my.app.recipe.model.database.RecipeRepository
import my.app.recipe.model.database.RecipeRoomDatabase

class RecipeApplication : Application() {

    private val database by lazy { RecipeRoomDatabase.getDatabase(this@RecipeApplication) }

    val repository by lazy { RecipeRepository(database.recipeDao()) }
}



