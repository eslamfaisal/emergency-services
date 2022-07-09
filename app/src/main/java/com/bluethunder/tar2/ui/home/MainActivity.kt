package com.bluethunder.tar2.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.databinding.ActivityMainBinding
import com.bluethunder.tar2.ui.getViewModelFactory
import com.bluethunder.tar2.ui.home.adapter.ViewPagerFragmentAdapter
import com.bluethunder.tar2.ui.home.fragments.CategorizedEmergencyCaseListFragment
import com.bluethunder.tar2.ui.home.fragments.HomeMapFragment
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel


class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<HomeViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()

    }

    private fun initViewPager() {
        val mutableFragmentList : MutableList<Fragment> = ArrayList()
        mutableFragmentList.add(HomeMapFragment())
        mutableFragmentList.add(CategorizedEmergencyCaseListFragment())
        binding.homeTabViewPager.adapter =
            ViewPagerFragmentAdapter(this, mutableFragmentList)
    }
}