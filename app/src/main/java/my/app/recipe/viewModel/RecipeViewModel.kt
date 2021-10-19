package my.app.recipe.viewModel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import my.app.recipe.model.database.RecipeRepository
import my.app.recipe.model.entities.Recipe

class RecipeViewModel( private val repository: RecipeRepository):ViewModel() {
    fun insert(recipe: Recipe) = viewModelScope.launch {

        repository.insertRecipeData(recipe)
    }

    val allDishesList: LiveData<List<Recipe>> = repository.allDishesList.asLiveData()

    fun update(dish: Recipe) = viewModelScope.launch {
        repository.updateFavDishData(dish)
    }

    val favoriteDishes: LiveData<List<Recipe>> = repository.favoriteDishes.asLiveData()

    fun delete(dish: Recipe) = viewModelScope.launch {
        // Call the repository function and pass the details.
        repository.deleteFavDishData(dish)
    }
    fun getFilteredList(value: String): LiveData<List<Recipe>> = repository.filteredListDishes(value).asLiveData()

}
    class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    }






