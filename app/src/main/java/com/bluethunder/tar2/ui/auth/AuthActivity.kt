package com.bluethunder.tar2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.ui.BaseActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.setAppLocale
import com.bluethunder.tar2.ui.splash.SplashActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys

class AuthActivity : BaseActivity() {

    val viewModel by viewModels<AuthViewModel> { getViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        findViewById<View>(R.id.currentLanguage).setOnClickListener {
            SharedHelper.getString(this, SharedHelperKeys.LANGUAGE_KEY, defaultValue = "en").let {
                if (it == "en") {
                    SharedHelper.putString(this, SharedHelperKeys.LANGUAGE_KEY, "ar")
                } else {
                    SharedHelper.putString(this, SharedHelperKeys.LANGUAGE_KEY, "en")
                }

                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}