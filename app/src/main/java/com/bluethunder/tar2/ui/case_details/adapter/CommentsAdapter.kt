package com.bluethunder.tar2.ui.case_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.CommentsListItemBinding
import com.bluethunder.tar2.ui.case_details.model.CommentModel
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    private val TAG = "CommentsAdapter"

    var commentsList: MutableList<CommentModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            CommentsListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commentModel = commentsList[holder.absoluteAdapterPosition]
        holder.bindingView.usernameTv.text = commentModel.userName
        holder.bindingView.commentTv.text = commentModel.comment

        commentModel.createdAt?.let {
            val timeAgo = TimeAgo()
            timeAgo.locale(holder.bindingView.root.context)
            holder.bindingView.dateTv.text = timeAgo.getTimeAgo(it)
        }

        val circularProgressDrawable =
            CircularProgressDrawable(holder.bindingView.usernameTv.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(holder.bindingView.usernameTv.context)
            .load(commentModel.userImage)
            .placeholder(circularProgressDrawable)
            .error(R.drawable.ic_small_profile_image_place_holder)
            .optionalTransform(CircleCrop())
            .into(holder.bindingView.profileImage)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: CommentsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: CommentsListItemBinding = binding
    }

    fun addNewData(comments: List<CommentModel>) {
        this.commentsList.addAll(0, comments)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.commentsList.clear()
        notifyDataSetChanged()
    }

}