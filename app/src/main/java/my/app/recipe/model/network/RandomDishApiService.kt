package my.app.recipe.model.network

import io.reactivex.rxjava3.core.Single
import my.app.recipe.model.entities.RandomDish
import my.app.recipe.utils.Constants
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RandomDishApiService {

    private val api = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(RandomDishAPI::class.java)

    fun getRandomDish(): Single<RandomDish.Recipes> {
        return api.getRandomDish(
                Constants.API_KEY_VALUE,
                Constants.LIMIT_LICENSE_VALUE,
                Constants.TAGS_VALUE,
                Constants.NUMBER_VALUE
        )
    }

}