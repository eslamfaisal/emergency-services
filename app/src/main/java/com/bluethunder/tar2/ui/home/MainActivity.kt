package com.bluethunder.tar2.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.databinding.ActivityMainBinding
import com.bluethunder.tar2.ui.getViewModelFactory
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel


class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<HomeViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}