package my.app.recipe.view.adapters

import android.content.Intent
import android.provider.SyncStateContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.app.recipe.R
import my.app.recipe.databinding.ItemDishLayoutBinding
import my.app.recipe.model.entities.Recipe
import my.app.recipe.utils.Constants
import my.app.recipe.utils.Constants.EXTRA_DISH_DETAILS
import my.app.recipe.view.activities.AddUpdateDish
import my.app.recipe.view.fragments.AllDishesFragment
import my.app.recipe.view.fragments.FavouriteDishesFragment

class RecipeAdapter(private val fragment : Fragment) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    private var dishes: List<Recipe> = listOf()

    class ViewHolder(view: ItemDishLayoutBinding) : RecyclerView.ViewHolder(view.root) {

        val ivDishImage = view.ivDishImage
        val tvTitle = view.tvDishTitle
        val ibMore = view.ibMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDishLayoutBinding =
            ItemDishLayoutBinding.inflate(LayoutInflater.from(fragment.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishes[position]


        Glide.with(fragment)
            .load(dish.image)
            .into(holder.ivDishImage)

        holder.tvTitle.text = dish.title

        holder.itemView.setOnClickListener {
            if (fragment is AllDishesFragment) {
                fragment.dishDetails(dish)
            } else if (fragment is FavouriteDishesFragment) {
                fragment.dishDetails(dish)
            }
        }
            if (fragment is AllDishesFragment) {
                holder.ibMore.visibility = View.VISIBLE
            } else if (fragment is FavouriteDishesFragment) {
                holder.ibMore.visibility = View.GONE
            }

            holder.ibMore.setOnClickListener {
                val popup = PopupMenu(fragment.context, holder.ibMore)

                popup.menuInflater.inflate(R.menu.menu_adapter, popup.menu)

                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.action_edit_dish) {
                        val intent =
                            Intent(fragment.requireActivity(), AddUpdateDish::class.java)
                        intent.putExtra(Constants.EXTRA_DISH_DETAILS, dish)
                        fragment.requireActivity().startActivity(intent)
                    } else if (it.itemId == R.id.action_delete_dish) {
                        if (fragment is AllDishesFragment) {
                            fragment.deleteStudent(dish)
                        }
                    }
                    true
                }

                popup.show()
            }
        }

    override fun getItemCount(): Int {
        return dishes.size
    }

    fun dishesList(list: List<Recipe>) {
        dishes = list
        notifyDataSetChanged()
    }


}

