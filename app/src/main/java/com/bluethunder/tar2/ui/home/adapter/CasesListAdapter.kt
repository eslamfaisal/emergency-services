package com.bluethunder.tar2.ui.home.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.databinding.CasesListItemBinding
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class CasesListAdapter(
    val interaction: CasesListInteractions
) :
    RecyclerView.Adapter<CasesListAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var myCases: MutableList<CaseModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            CasesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val caseModel = myCases[holder.absoluteAdapterPosition]
        caseModel.title?.let { holder.bindingView.titleTv.text = it }
        caseModel.createdAt.let {
            holder.bindingView.dateTv.text = DateFormat.format("yyyy-MM-dd hh:mm:ss a", it)
        }


        val circularProgressDrawable =
            CircularProgressDrawable(holder.bindingView.mainImageView.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(holder.bindingView.mainImageView.context)
            .load(myCases[holder.absoluteAdapterPosition].mainImage)
            .placeholder(circularProgressDrawable)
            .into(holder.bindingView.mainImageView)

        Glide.with(holder.bindingView.profileImage.context)
            .load(myCases[holder.absoluteAdapterPosition].userImage)
            .placeholder(circularProgressDrawable)
            .optionalTransform(CircleCrop())
            .into(holder.bindingView.profileImage)
    }


    override fun getItemCount(): Int {
        return myCases.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: CasesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: CasesListItemBinding = binding
    }

    fun addNewData(notificationsList: MutableList<CaseModel>) {
        this.myCases.addAll(notificationsList)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.myCases.clear()
        notifyDataSetChanged()
    }

    interface CasesListInteractions {
        fun onCasenClicked(caseModel: CaseModel)
    }

}