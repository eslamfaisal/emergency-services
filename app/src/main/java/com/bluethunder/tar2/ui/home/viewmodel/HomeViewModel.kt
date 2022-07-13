package com.bluethunder.tar2.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
    }

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _onMapSelected = MutableLiveData(true)
    val onMapSelected: LiveData<Boolean> = _onMapSelected

    private val _caseListSelected = MutableLiveData(false)
    val caseListSelected: LiveData<Boolean> = _caseListSelected

    fun refresh() {

        _dataLoading.value = true
        viewModelScope.launch {
            _dataLoading.value = false
        }
    }

}
