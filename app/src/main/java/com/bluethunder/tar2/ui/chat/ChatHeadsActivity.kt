package com.bluethunder.tar2.ui.chat

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluethunder.tar2.databinding.ActivityChatHeadsBinding
import com.bluethunder.tar2.model.Status.*
import com.bluethunder.tar2.ui.chat.adapter.ChatHeadAdapter
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.ui.chat.viewmodel.ChatHeadViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showLoadingDialog

class ChatHeadsActivity : AppCompatActivity() , ChatHeadAdapter.ChatHeadInteractions {

    private val viewModel by viewModels<ChatHeadViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityChatHeadsBinding
    lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatHeadsBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel

        setContentView(binding.root)

        initViews()
        initViewModel()
    }


    private fun initViewModel() {
        viewModel.getChatHeads()
        viewModel.addedChatHeads.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    Log.d(TAG, "initViewModel: ${resource.data!!.size}")
                    chatHeadAdapter.addNewData(resource.data!!)
                    progressDialog.dismiss()
                }
                ERROR -> {
                    progressDialog.dismiss()
                }
                LOADING -> {
                    progressDialog.show()
                }
                EMPTY -> {
                    progressDialog.dismiss()
                }
            }
        }
    }

    lateinit var chatHeadAdapter: ChatHeadAdapter
    private fun initViews() {
        progressDialog = showLoadingDialog()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        chatHeadAdapter = ChatHeadAdapter(this)
        binding.chatHeadRecyclerView.apply {
            adapter = chatHeadAdapter
            layoutManager = LinearLayoutManager(this@ChatHeadsActivity)
        }
    }

    override fun onChatHeadClicked(chatHead: ChatHead) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.CASE_EXTRA_KEY, chatHead)
            putExtra(ChatActivity.CHAT_HEAD_EXTRA_KEY, chatHead)
        })
    }

    companion object {
        private const val TAG = "ChatHeadsActivity"
    }

}