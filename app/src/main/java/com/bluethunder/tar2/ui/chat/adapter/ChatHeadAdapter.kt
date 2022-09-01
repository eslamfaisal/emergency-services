package com.bluethunder.tar2.ui.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ChatHeadItemBinding
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide

class ChatHeadAdapter(
    val interaction: ChatHeadInteractions
) :
    RecyclerView.Adapter<ChatHeadAdapter.ViewHolder>() {
    private val TAG = "NotificationCenterAdapt"

    var chatHeads: MutableList<ChatHead> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ChatHeadItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatHead = chatHeads[holder.absoluteAdapterPosition]
        chatHead.caseTitle?.let { holder.bindingView.titleTv.text = it }
        chatHead.lastMessage?.let { holder.bindingView.messageContent.text = it }
        chatHead.lastMessageAt.let {
            val timeago = TimeAgo()
            timeago.locale(holder.bindingView.root.context)
            holder.bindingView.date.text = timeago.getTimeAgo(it)
        }

        val circularProgressDrawable =
            CircularProgressDrawable(holder.bindingView.mainImageView.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(holder.bindingView.mainImageView.context)
            .asBitmap()
            .placeholder(circularProgressDrawable)
            .load(chatHeads[holder.absoluteAdapterPosition].caseImage)
            .override(200, 200)
            .error(holder.bindingView.mainImageView.context.resources.getDrawable(R.drawable.ic_place_holder))
            .into(holder.bindingView.mainImageView)

        holder.bindingView.root.setOnClickListener {
            interaction.onChatHeadClicked(chatHead)
        }
    }

    override fun getItemCount(): Int {
        return chatHeads.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(binding: ChatHeadItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindingView: ChatHeadItemBinding = binding
    }

    fun addNewData(chatHeads: MutableList<ChatHead>) {
        this.chatHeads.removeAll(chatHeads)
        this.chatHeads.addAll(chatHeads)
        this.chatHeads.sortByDescending { it.lastMessageAt }
        notifyDataSetChanged()
    }

    fun modifiedList(data: MutableList<ChatHead>) {
        this.chatHeads.removeAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        this.chatHeads.clear()
        notifyDataSetChanged()
    }

    interface ChatHeadInteractions {
        fun onChatHeadClicked(ChatHead: ChatHead)
    }

}