package com.bluethunder.tar2.ui

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(this, this)
}