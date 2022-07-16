package com.bluethunder.tar2.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.cloud_db.CloudDBWrapper
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.bluethunder.tar2.databinding.ActivitySplashBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys.IS_LOGGED_IN

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({
            runOnUiThread {
                openCloudDBZones()
            }
        }, 1000)
    }

    private fun openCloudDBZones() {
        CloudDBWrapper.setStorageLocation(this)
        CloudDBWrapper.createObjectType()
        CloudDBWrapper.openUsersCloudDBZoneV2 {
            if (it) {
                CloudStorageWrapper.initStorage(this)
                checkLogin()
            }
        }
    }

    private fun checkLogin() {
        val isLoggedIn = SharedHelper.getBoolean(this, IS_LOGGED_IN)
        if (isLoggedIn) {
            openHomeActivity()
        } else {
            openSplashActivity()
        }
    }

    private fun openHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun openSplashActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}