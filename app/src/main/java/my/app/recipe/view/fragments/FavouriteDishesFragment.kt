package my.app.recipe.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import my.app.recipe.R
import my.app.recipe.application.RecipeApplication
import my.app.recipe.databinding.FragmentFavouriteDishesBinding
import my.app.recipe.model.entities.Recipe
import my.app.recipe.view.activities.MainActivity
import my.app.recipe.view.adapters.RecipeAdapter
import my.app.recipe.viewModel.DashboardViewModel
import my.app.recipe.viewModel.RecipeViewModel
import my.app.recipe.viewModel.RecipeViewModelFactory

class FavouriteDishesFragment : Fragment() {

    private var mBinding: FragmentFavouriteDishesBinding? = null


    private val mFavDishViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory((requireActivity().application as RecipeApplication).repository)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentFavouriteDishesBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner) { dishes ->
            dishes.let {


                mBinding!!.rvFavoriteDishesList.layoutManager =
                        GridLayoutManager(requireActivity(), 2)

                val adapter = RecipeAdapter(this@FavouriteDishesFragment)

                mBinding!!.rvFavoriteDishesList.adapter = adapter

                if (it.isNotEmpty()) {
                    mBinding!!.rvFavoriteDishesList.visibility = View.VISIBLE
                    mBinding!!.tvNoFavoriteDishesAvailable.visibility = View.GONE

                    adapter.dishesList(it)
                } else {
                    mBinding!!.rvFavoriteDishesList.visibility = View.GONE
                    mBinding!!.tvNoFavoriteDishesAvailable.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.showBottomNavigationView()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }


    fun dishDetails(favDish: Recipe) {


        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }


        findNavController()
                .navigate(FavouriteDishesFragmentDirections.actionFavouriteDishesToDishDetails(favDish))
    }

}