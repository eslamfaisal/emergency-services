package com.bluethunder.tar2.ui.chat

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.databinding.ActivityChatHeadsBinding
import com.bluethunder.tar2.model.Status.*
import com.bluethunder.tar2.ui.chat.viewmodel.ChatHeadViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.views.setupRefreshLayout

class ChatHeadsActivity : AppCompatActivity() {

    private val viewModel by viewModels<ChatHeadViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityChatHeadsBinding
    lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatHeadsBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel

        setupRefreshLayout(binding.refreshLayout)
        setContentView(binding.root)

        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.getChatHeads()
        viewModel.chatHeads.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    Log.d(TAG, "initViewModel: ${resource.data!!.size}")
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

    private fun initViews() {
        progressDialog = showLoadingDialog()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    companion object {
        private const val TAG = "ChatHeadsActivity"
    }
}