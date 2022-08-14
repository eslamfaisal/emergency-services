package com.bluethunder.tar2.ui.case_details.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.case_details.model.CommentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class CaseDetailsViewModel : ViewModel() {

    companion object {
        private val TAG = CaseDetailsViewModel::class.java.simpleName
    }

    private val _onSelectedTabIndex = MutableLiveData(0)
    val onSelectedTabIndex: LiveData<Int> = _onSelectedTabIndex

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _commentsList = MutableLiveData<Resource<List<CommentModel>>>()
    val commentsList: LiveData<Resource<List<CommentModel>>> = _commentsList

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

    fun listenToComments(caseId: String) {
        FirebaseFirestore.getInstance().collection("cases")
            .document(caseId)
            .collection("comments")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "listenToComments: ", error)
                } else {
                    val list: MutableList<CommentModel> = ArrayList()
                    value!!.documentChanges.forEach { document ->
                        val comment = document.document.toObject(CommentModel::class.java)
                        list.add(comment)
                    }
                }
            }
    }

}
