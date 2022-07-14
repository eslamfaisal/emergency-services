package com.bluethunder.tar2.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.ui.BaseActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.getViewModelFactory

class AuthActivity : BaseActivity() {

    val viewModel by viewModels<AuthViewModel> { getViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}