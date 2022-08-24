package com.bluethunder.tar2.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityMainBinding
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.adapter.ViewPagerFragmentAdapter
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.ui.home.fragments.HomeMapFragment
import com.bluethunder.tar2.ui.home.fragments.MenuFragment
import com.bluethunder.tar2.ui.home.fragments.MyCasesFragment
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel
import com.bluethunder.tar2.ui.home.viewmodel.NotificationsViewModel
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

    }

    private val viewModel by viewModels<HomeViewModel> { getViewModelFactory() }
    private val notificationViewModel by viewModels<NotificationsViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initViews()
        initViewModel()
//        setTransparentStatusBar()
    }


    fun setTransparentStatusBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun initViews() {
        initViewPager()
        binding.newCaseFabBtn.setOnClickListener {
            goToCreateNewCase()
        }
    }

    private fun goToCreateNewCase() {
        val intent = Intent(this, EditCaseActivity::class.java)
        intent.putExtra(EditCaseActivity.EXTRA_IS_NEW_CASE, true)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun initViewPager() {
        val mutableFragmentList: MutableList<Fragment> = ArrayList()
        mutableFragmentList.add(HomeMapFragment())
        mutableFragmentList.add(CasesListFragment())
        mutableFragmentList.add(MyCasesFragment())
        mutableFragmentList.add(MenuFragment())
        binding.homeTabViewPager.adapter =
            ViewPagerFragmentAdapter(this, mutableFragmentList)
        binding.homeTabViewPager.offscreenPageLimit = 4
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViewModel() {
        notificationViewModel.getToken()
        // region tab layout
        viewModel.onSelectedTabIndex.observe(this) { position ->
            binding.homeTabViewPager.currentItem = position

            binding.mapTabIcon.setImageDrawable(
                getDrawable(
                    if (position == 0) R.drawable.ic_map_tab_selected else R.drawable.ic_map_tab_un_selected
                )
            )
            binding.mapTabText.visibility = if (position == 0) View.VISIBLE else View.GONE


            binding.caseListTabIcon.setImageDrawable(
                getDrawable(
                    if (position == 1) R.drawable.ic_case_list_tab_selected else R.drawable.ic_case_list_tab_un_selected
                )
            )
            binding.caseListTabText.visibility = if (position == 1) View.VISIBLE else View.GONE


            binding.myCasesTabIcon.setImageDrawable(
                getDrawable(
                    if (position == 2) R.drawable.ic_my_cases_selected else R.drawable.ic_my_cases_un_selected
                )
            )
            binding.myCasesTabText.visibility = if (position == 2) View.VISIBLE else View.GONE

            binding.menuTabIcon.setImageDrawable(
                getDrawable(
                    if (position == 3) R.drawable.ic_more_tab_selected else R.drawable.ic_more_tab_un_selected
                )
            )
            binding.menuTabText.visibility = if (position == 3) View.VISIBLE else View.GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EditCaseActivity.REQUEST_DEVICE_SETTINGS -> {
//                recreate()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

}