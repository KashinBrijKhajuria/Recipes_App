package my.app.recipe.model.network

import io.reactivex.rxjava3.core.Single
import my.app.recipe.model.entities.RandomDish
import my.app.recipe.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomDishAPI {

    @GET(Constants.API_ENDPOINT)
    fun getRandomDish(

            @Query(Constants.API_KEY) apiKey: String,
            @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
            @Query(Constants.TAGS) tags: String,
            @Query(Constants.NUMBER) number: Int
    ): Single<RandomDish.Recipes>


}