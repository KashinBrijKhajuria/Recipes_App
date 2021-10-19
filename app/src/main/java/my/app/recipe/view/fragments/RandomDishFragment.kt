package my.app.recipe.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import my.app.recipe.R
import my.app.recipe.application.RecipeApplication
import my.app.recipe.databinding.FragmentRandomDishBinding
import my.app.recipe.model.entities.RandomDish
import my.app.recipe.model.entities.Recipe
import my.app.recipe.utils.Constants
import my.app.recipe.viewModel.NotificationsViewModel
import my.app.recipe.viewModel.RandomDishViewModel
import my.app.recipe.viewModel.RecipeViewModel
import my.app.recipe.viewModel.RecipeViewModelFactory

class RandomDishFragment : Fragment() {

    private var mBinding: FragmentRandomDishBinding? = null

    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRandomDishBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mRandomDishViewModel =
                ViewModelProvider(this).get(RandomDishViewModel::class.java)

        mRandomDishViewModel.getRandomDishFromAPI()

        randomDishViewModelObserver()

        mBinding!!.srlRandomDish.setOnRefreshListener {

            mRandomDishViewModel.getRandomDishFromAPI()
        }
    }


    private fun randomDishViewModelObserver() {

        mRandomDishViewModel.randomDishResponse.observe(
                viewLifecycleOwner,
                Observer { randomDishResponse ->
                    randomDishResponse?.let {
                        Log.i("Random Dish Response", "${randomDishResponse.recipes[0]}")

                        if (mBinding!!.srlRandomDish.isRefreshing) {
                            mBinding!!.srlRandomDish.isRefreshing = false
                        }

                        setRandomDishResponseInUI(randomDishResponse.recipes[0])
                    }
                })

        mRandomDishViewModel.randomDishLoadingError.observe(
                viewLifecycleOwner,
                Observer { dataError ->
                    dataError?.let {
                        Log.i("Random Dish API Error", "$dataError")

                        if (mBinding!!.srlRandomDish.isRefreshing) {
                            mBinding!!.srlRandomDish.isRefreshing = false
                        }
                    }
                })

        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, Observer { loadRandomDish ->
            loadRandomDish?.let {
                Log.i("Random Dish Loading", "$loadRandomDish")


                if (loadRandomDish && !mBinding!!.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog()
                } else {
                    hideProgressDialog()
                }

            }
        })
    }


    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {


        Glide.with(requireActivity())
                .load(recipe.image)
                .centerCrop()
                .into(mBinding!!.ivDishImage)

        mBinding!!.tvTitle.text = recipe.title

        var dishType: String = "other"

        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0] as String
            mBinding!!.tvType.text = dishType
        }


        mBinding!!.tvCategory.text = "Other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            if (ingredients.isEmpty()) {
                ingredients = value.original
            } else {
                ingredients = ingredients + ", \n" + value.original
            }
        }

        mBinding!!.tvIngredients.text = ingredients


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions,
                    Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("DEPRECATION")
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        mBinding!!.tvCookingTime.text =
                resources.getString(
                        R.string.lbl_estimate_cooking_time,
                        recipe.readyInMinutes.toString()
                )

        mBinding!!.ivFavoriteDish.setImageDrawable(
                ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_unselected
                )
        )

        var addedToFavorite = false

        mBinding!!.ivFavoriteDish.setOnClickListener {

            if (addedToFavorite) {
                Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.msg_already_added_to_favorites),
                        Toast.LENGTH_SHORT
                ).show()
            } else {

                val randomDishDetails = Recipe(
                        recipe.image,
                        Constants.DISH_IMAGE_SOURCE_ONLINE,
                        recipe.title,
                        dishType,
                        "Other",
                        ingredients,
                        recipe.readyInMinutes.toString(),
                        recipe.instructions,
                        true
                )

                val mFavDishViewModel: RecipeViewModel by viewModels {
                    RecipeViewModelFactory((requireActivity().application as RecipeApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)

                addedToFavorite = true

                mBinding!!.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_favorite_selected
                        )
                )

                Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.msg_added_to_favorites),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }


    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireActivity())

        mProgressDialog?.let {

            it.setContentView(R.layout.dialog_custom_progress)


            it.show()
        }
    }

    private fun hideProgressDialog() {
        mProgressDialog?.let {
            it.dismiss()
        }
    }

}