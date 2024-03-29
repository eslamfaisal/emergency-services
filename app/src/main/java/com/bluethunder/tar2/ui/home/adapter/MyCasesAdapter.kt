package com.bluethunder.tar2.ui.home.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.MyCasesItemBinding
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.home.model.CaseStatus
import com.bumptech.glide.Glide

class MyCasesAdapter(
    val interaction: MyCasesInteractions
) :
    RecyclerView.Adapter<MyCasesAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var myCases: MutableList<CaseModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            MyCasesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val caseModel = myCases[holder.absoluteAdapterPosition]
        caseModel.title?.let { holder.bindingView.titleTv.text = it }
        caseModel.upVotesCount.let { holder.bindingView.upVotesTv.text = it.toString() }
        caseModel.commentsCount.let { holder.bindingView.commentsTv.text = it.toString() }
        caseModel.viewsCount.let { holder.bindingView.viewsTv.text = it.toString() }
        caseModel.status.let {
            when (it) {
                CaseStatus.Published.name -> {
                    holder.bindingView.statusTv.text =
                        holder.bindingView.statusTv.context.getString(R.string.published)
                    holder.bindingView.statusTv.setTextColor(
                        ContextCompat.getColor(
                            holder.bindingView.statusTv.context,
                            R.color.colorPrimary
                        )
                    )
                    setCardColor(holder, color = R.color.color_published)
                }
                CaseStatus.UnPublished.name -> {
                    holder.bindingView.statusTv.text =
                        holder.bindingView.statusTv.context.getString(R.string.un_published)

                    holder.bindingView.statusTv.setTextColor(
                        ContextCompat.getColor(
                            holder.bindingView.statusTv.context,
                            R.color.dark_red
                        )
                    )
                    setCardColor(holder, color = R.color.color_unpublished)
                }
            }
        }

        val circularProgressDrawable = CircularProgressDrawable(holder.bindingView.mainImageView.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(holder.bindingView.mainImageView.context)
            .asBitmap()
            .placeholder(circularProgressDrawable)
            .load(myCases[holder.absoluteAdapterPosition].mainImage)
            .override(200, 200)
            .error(holder.bindingView.mainImageView.context.resources.getDrawable(R.drawable.ic_place_holder))
            .into(holder.bindingView.mainImageView)

        holder.bindingView.root.setOnClickListener {
            interaction.onMyCaseClicked(caseModel)
        }
    }

    private fun setCardColor(holder: ViewHolder, color: Int = R.color.colorPrimary) {
        holder.bindingView.statusCard.setCardBackgroundColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    holder.bindingView.statusCard.context,
                    color
                )
            )
        )
    }

    override fun getItemCount(): Int {
        return myCases.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: MyCasesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: MyCasesItemBinding = binding
    }

    fun addNewData(notificationsList: MutableList<CaseModel>) {
        this.myCases.removeAll(notificationsList)
        this.myCases.addAll(notificationsList)
        notifyDataSetChanged()
    }

    fun addDeleteData(data: MutableList<CaseModel>) {
        this.myCases.removeAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.myCases.clear()
        notifyDataSetChanged()
    }

    interface MyCasesInteractions {
        fun onMyCaseClicked(caseModel: CaseModel)
    }

}