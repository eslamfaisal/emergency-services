package com.bluethunder.tar2.ui.extentions

import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.ui.ViewModelFactory

fun AppCompatActivity.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(this, this)
}