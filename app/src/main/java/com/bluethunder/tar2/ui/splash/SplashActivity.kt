package com.bluethunder.tar2.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.bluethunder.tar2.databinding.ActivitySplashBinding
import com.bluethunder.tar2.model.NotificationType
import com.bluethunder.tar2.model.remot_config.EnabledCategoriesConfig
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.chat.ChatActivity
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.setAppLocale
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.ui.splash.viewmodel.SplashViewModel
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys.IS_LOGGED_IN
import com.bluethunder.tar2.utils.SharedHelperKeys.LANGUAGE_KEY
import com.bluethunder.tar2.utils.SharedHelperKeys.USER_DATA
import com.google.gson.Gson
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.applinking.AGConnectAppLinking
import com.huawei.agconnect.remoteconfig.AGConnectConfig

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    private val viewModel by viewModels<SplashViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLanguage()

        binding.root.postDelayed({
            runOnUiThread {
                try {
                    openCloudDBZones()
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening cloud db zones", e)
                }
            }
        }, 1000)
    }

    private fun initLanguage() {
        SharedHelper.getString(this, LANGUAGE_KEY, defaultValue = "en")?.let {
            SessionConstants.currentLanguage = it
            setAppLocale(this, it)
        }
    }

    private fun openCloudDBZones() {
        AGConnectInstance.initialize(this)
        CloudStorageWrapper.initStorage(this)
        getRemoteConfigData()

    }

    private fun getRemoteConfigData() {
        AGConnectConfig.getInstance().fetch().addOnCompleteListener {
            Log.d(TAG, "getRemoteConfigData: ${it.result}")
            Log.d(TAG, "getRemoteConfigData: ${it.result.getValueAsString("categories")}")
            try {
                SessionConstants.enabledCategories = Gson().fromJson(
                    it.result.getValueAsString("categories"),
                    EnabledCategoriesConfig::class.java
                ).categories
            } catch (e: Exception) {
            }
            checkLogin()
        }
    }

    private fun checkLogin() {
        val isLoggedIn = SharedHelper.getBoolean(this, IS_LOGGED_IN)
        if (isLoggedIn) {
            currentLoggedInUserModel = Gson().fromJson(
                SharedHelper.getString(this, USER_DATA), UserModel::class.java
            )
            Log.d(TAG, "checkLogin: userId = ${currentLoggedInUserModel?.id}")
            openHomeActivity()
            checkOpenFrom()

        } else {
            openAuthActivity()
        }
    }

    private fun checkOpenFrom() {
        getDeepLink()
        checkNotificationIntent()
    }

    private fun checkNotificationIntent() {

        try {
            if (intent.getStringExtra("type") == NotificationType.Chat.name) {
                val chatHead = intent.getSerializableExtra(ChatActivity.CHAT_HEAD_EXTRA_KEY)
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.CHAT_HEAD_EXTRA_KEY, chatHead)
                })
            } else {
                val casId = intent.getStringExtra("case_id")
                Log.d(TAG, "checkNotificationIntent:case_id =  ${casId}")
                casId?.let {
                    viewModel.getCaseDetailsAndOpenIt(this, it)
                }
            }
        } catch (e: Exception) {
        }

    }

    private fun openHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun getDeepLink() {
        AGConnectAppLinking.getInstance()
            .getAppLinking(this).addOnSuccessListener {
                try {
                    val deepLink = it.deepLink
                    if (deepLink != null) {
                        Log.d(TAG, "getDeepLink: deepLink = $deepLink")
                        openDeepLinkActivity(deepLink)
                    } else
                        finish()
                } catch (e: Exception) {
                    Log.e(TAG, "getDeepLink: error", e)
                    finish()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "getDeepLink: error = ${e.message}")
                finish()
            }
    }

    @Throws(Exception::class)
    private fun openDeepLinkActivity(deepLink: Uri) {
        Log.d(TAG, "openDeepLinkActivity: deepLink = $deepLink")
        val caseId = deepLink.toString().split("/").last()
        Log.d(TAG, "openDeepLinkActivity: caseId = $caseId")
        viewModel.getCaseDetailsAndOpenIt(this, caseId)
    }

    private fun openAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

}