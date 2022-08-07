package com.bluethunder.tar2.ui.home.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants.currentLanguage
import com.bluethunder.tar2.databinding.CategoryItemBinding
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel

class CategoriesAdapter(
    val interaction: CategoryInteractions
) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var categoriesList: MutableList<CaseCategoryModel> = ArrayList()
    var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoriesList[holder.absoluteAdapterPosition]
        when (currentLanguage) {
            "en" -> {
                holder.bindingView.categoryNameIv.text = category.nameEn
            }
            "ar" -> {
                holder.bindingView.categoryNameIv.text = category.nameAr
            }
        }

        if (selectedIndex == holder.absoluteAdapterPosition) {
            holder.bindingView.categoryNameIv.setTextColor(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorBlack
                )
            )
            holder.bindingView.categoryCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorWhite
                )
            )
            holder.bindingView.categoryIconIv.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorPrimary
                )
            )
        } else {
            holder.bindingView.categoryNameIv.setTextColor(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorGreyLight
                )
            )
            holder.bindingView.categoryCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorGreyVeryLight
                )
            )
            holder.bindingView.categoryIconIv.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    holder.bindingView.categoryNameIv.context,
                    R.color.colorGreyLight
                )
            )
        }
        holder.bindingView.root.setOnClickListener {
            selectedIndex = holder.absoluteAdapterPosition
            interaction.onCategoryClicked(category)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: CategoryItemBinding = binding
    }

    fun addNewData(list: MutableList<CaseCategoryModel>) {
        this.categoriesList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.categoriesList.clear()
        notifyDataSetChanged()
    }

    interface CategoryInteractions {
        fun onCategoryClicked(category: CaseCategoryModel)
    }

}