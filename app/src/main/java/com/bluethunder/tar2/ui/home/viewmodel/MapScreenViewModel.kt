package com.bluethunder.tar2.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class MapScreenViewModel : ViewModel() {

    companion object {
        private val TAG = MapScreenViewModel::class.java.simpleName
    }

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun refresh() {

        _dataLoading.value = true
        viewModelScope.launch {
            _dataLoading.value = false
        }
    }

}
