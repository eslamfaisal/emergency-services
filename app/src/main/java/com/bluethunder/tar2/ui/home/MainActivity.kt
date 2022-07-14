package com.bluethunder.tar2.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityMainBinding
import com.bluethunder.tar2.ui.getViewModelFactory
import com.bluethunder.tar2.ui.home.adapter.ViewPagerFragmentAdapter
import com.bluethunder.tar2.ui.home.fragments.CategorizedEmergencyCaseListFragment
import com.bluethunder.tar2.ui.home.fragments.HomeMapFragment
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<HomeViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initViews()
        initViewModel()
    }

    private fun initViews() {
        initViewPager()

    }

    private fun initViewModel() {

        // region tab layout
        viewModel.onSelectedTabIndex.observe(this) { position ->
            binding.homeTabViewPager.currentItem = position
            DrawableCompat.setTint(
                DrawableCompat.wrap(binding.mapTabIcon.drawable),
                ContextCompat.getColor(
                    this,
                    if (position == 0) R.color.colorPrimary else R.color.colorGreyLight
                )
            )
            DrawableCompat.setTint(
                DrawableCompat.wrap(binding.caseListTabIcon.drawable),
                ContextCompat.getColor(
                    this,
                    if (position == 1) R.color.colorPrimary else R.color.colorGreyLight
                )
            )
            DrawableCompat.setTint(
                DrawableCompat.wrap(binding.myCasesTabIcon.drawable),
                ContextCompat.getColor(
                    this,
                    if (position == 2) R.color.colorPrimary else R.color.colorGreyLight
                )
            )
            DrawableCompat.setTint(
                DrawableCompat.wrap(binding.menuTabIcon.drawable),
                ContextCompat.getColor(
                    this,
                    if (position == 3) R.color.colorPrimary else R.color.colorGreyLight
                )
            )
        }
        // endregion

    }

    private fun initViewPager() {
        val mutableFragmentList: MutableList<Fragment> = ArrayList()
        mutableFragmentList.add(HomeMapFragment())
        mutableFragmentList.add(CategorizedEmergencyCaseListFragment())
        mutableFragmentList.add(HomeMapFragment())
        mutableFragmentList.add(CategorizedEmergencyCaseListFragment())
        binding.homeTabViewPager.adapter =
            ViewPagerFragmentAdapter(this, mutableFragmentList)


    }
}