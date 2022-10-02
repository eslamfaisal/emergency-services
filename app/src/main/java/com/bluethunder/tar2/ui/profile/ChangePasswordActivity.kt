package com.bluethunder.tar2.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.profile.viewmodel.ChangePasswordViewModel

class ChangePasswordActivity : AppCompatActivity() {

    val viewModel by viewModels<ChangePasswordViewModel> { getViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }
}