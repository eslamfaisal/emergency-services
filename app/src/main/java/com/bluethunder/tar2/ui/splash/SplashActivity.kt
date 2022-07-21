package com.bluethunder.tar2.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.cloud_db.CloudDBWrapper
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.bluethunder.tar2.databinding.ActivitySplashBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.extentions.setAppLocale
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys.IS_LOGGED_IN
import com.bluethunder.tar2.utils.SharedHelperKeys.LANGUAGE_KEY
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import java.io.File

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

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
                    e.printStackTrace()
                    deleteCache(this)
                    recreate()
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
        AGConnectCloudDB.initialize(this)
        CloudDBWrapper.setStorageLocation(this)
        CloudDBWrapper.createObjectType()
        CloudDBWrapper.openUsersCloudDBZoneV2 {
            if (it) {
                CloudStorageWrapper.initStorage(this)
                checkLogin()
            } else {
                Log.d(TAG, "openCloudDBZones: failed to open cloud db zone recreate then")
                deleteCache(this)
                recreate()
            }
        }
    }

    private fun checkLogin() {
        val isLoggedIn = SharedHelper.getBoolean(this, IS_LOGGED_IN)
        if (isLoggedIn) {
            openHomeActivity()
        } else {
            openAuthActivity()
        }
    }

    private fun openHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun openAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }
}