package com.bluethunder.tar2.ui.case_details.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class CaseDetailsViewModel : ViewModel() {

    companion object {
        private val TAG = CaseDetailsViewModel::class.java.simpleName
    }

    private val _onSelectedTabIndex = MutableLiveData(0)
    val onSelectedTabIndex: LiveData<Int> = _onSelectedTabIndex

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun refresh() {

        _dataLoading.value = true
        viewModelScope.launch {
            _dataLoading.value = false
        }
    }

    fun setOnMapSelected(index: Int) {
        Log.d(TAG, "setOnMapSelected: $index")
        viewModelScope.launch {
            _onSelectedTabIndex.value = index
        }

    }

    fun listenToComments() {

    }

}
