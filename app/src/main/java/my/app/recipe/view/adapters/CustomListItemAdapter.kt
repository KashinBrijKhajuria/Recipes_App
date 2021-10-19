package my.app.recipe.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import my.app.recipe.databinding.ItemCustomListLayoutBinding
import my.app.recipe.view.activities.AddUpdateDish
import my.app.recipe.view.fragments.AllDishesFragment

class CustomListItemAdapter(private val activity : Activity,
                            private val fragment: Fragment?,
                            private val listItems :List<String>,
                            private val selection : String) : RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>(){

    class ViewHolder( view : ItemCustomListLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        val tvText = view.tvText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemCustomListLayoutBinding = ItemCustomListLayoutBinding
            .inflate(LayoutInflater.from(activity) , parent , false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItems[position]
        holder.tvText.text = item

        holder.itemView.setOnClickListener {

            if (activity is AddUpdateDish) {
                activity.selectedListItem(item, selection)
            }
            if (fragment is AllDishesFragment) {
                fragment.filterSelection(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

}