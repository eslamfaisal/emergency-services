package com.bluethunder.tar2.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.databinding.ActivitySettingsBinding
import com.bluethunder.tar2.ui.home.fragments.SelectLanguageBottomSheet
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys.NOTIFICATION_ENABLED

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.notificationSwitch.isChecked =
            SharedHelper.getBoolean(this, NOTIFICATION_ENABLED, defaultValue = true)

        binding.notificationSwitch.setOnClickListener {
            val enabled = binding.notificationSwitch.isChecked
            SharedHelper.putBoolean(
                this,
                NOTIFICATION_ENABLED,
                enabled
            )
        }

        binding.selectLanguage.setOnClickListener {
            val languageSheet = SelectLanguageBottomSheet()
            languageSheet.show(supportFragmentManager, "select_language")
        }
    }


    companion object {
        private const val TAG = "SettingsActivity"
    }
}