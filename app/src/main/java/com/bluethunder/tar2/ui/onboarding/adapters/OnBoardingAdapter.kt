package com.bluethunder.tar2.ui.onboarding.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluethunder.tar2.databinding.OnBoardingItemBinding
import com.bluethunder.tar2.ui.onboarding.model.BoardingModel

class OnBoardingAdapter :
    RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var boardingItem: MutableList<BoardingModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            OnBoardingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val caseModel = boardingItem[holder.absoluteAdapterPosition]

        holder.bindingView.boardingTitle.text = caseModel.title
        holder.bindingView.boardingDesc.text = caseModel.description
        holder.bindingView.boardingImg.setImageDrawable(
            holder.bindingView.boardingImg.context.resources.getDrawable(
                caseModel.image
            )
        )

    }

    override fun getItemCount(): Int {
        return boardingItem.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: OnBoardingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: OnBoardingItemBinding = binding
    }

    fun setData(data: MutableList<BoardingModel>) {
        boardingItem.addAll(data)
        notifyDataSetChanged()
    }
}