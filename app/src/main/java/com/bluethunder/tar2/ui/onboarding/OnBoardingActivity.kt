package com.bluethunder.tar2.ui.onboarding

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityOnBoardingBinding
import com.bluethunder.tar2.ui.onboarding.adapters.OnBoardingAdapter
import com.bluethunder.tar2.ui.onboarding.model.BoardingModel

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
            }
        })
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

    companion object {
        private const val TAG = "OnBoardingActivity"
    }
}