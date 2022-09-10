package com.bluethunder.tar2.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityOnBoardingBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.onboarding.adapters.OnBoardingAdapter
import com.bluethunder.tar2.ui.onboarding.model.BoardingModel
import com.bluethunder.tar2.ui.splash.SplashActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        val pagerAdapter = OnBoardingAdapter()
        pagerAdapter.setData(getData())
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "onPageSelected: selected page = ${position}")
                if (position == 0) {
                    setFirstPageStat()
                } else {
                    setSecondPageState()
                }
            }

        })
        setFirstPageStat()

        binding.currentLanguage.setOnClickListener {
            changeLanguage()
        }

        binding.btnNext.setOnClickListener {
            binding.viewPager.currentItem = 2
        }
        binding.btnGetStarted.setOnClickListener {
            goToAuthActivity()
        }
        binding.btnSkip.setOnClickListener {
            goToAuthActivity()
        }

    }

    private fun goToAuthActivity() {
        SharedHelper.putBoolean(this, SharedHelperKeys.ON_BOARDING_SHOW, true)
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun setFirstPageStat() {
        binding.indicatorImg.setImageDrawable(getDrawable(R.drawable.boarding_indicators_1))
        binding.btnNext.visibility = View.VISIBLE
        binding.btnGetStarted.visibility = View.GONE
    }

    private fun setSecondPageState() {
        binding.indicatorImg.setImageDrawable(getDrawable(R.drawable.border_indicators_2))
        binding.btnNext.visibility = View.GONE
        binding.btnGetStarted.visibility = View.VISIBLE
    }

    fun getData(): MutableList<BoardingModel> {
        val data: MutableList<BoardingModel> = ArrayList()
        data.add(
            BoardingModel(
                getString(R.string.boarding_title_text_1),
                getString(R.string.boarding_description_text_1),
                R.drawable.ic_boarding_img_1
            )
        )
        data.add(
            BoardingModel(
                getString(R.string.boarding_title_text_2),
                getString(R.string.boarding_description_text_2),
                R.drawable.ic_boarding_img_2
            )
        )

        return data
    }

    fun changeLanguage() {
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

    companion object {
        private const val TAG = "OnBoardingActivity"
    }
}