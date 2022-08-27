package com.bluethunder.tar2.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class MyCasesViewModel : ViewModel() {

    companion object {
        private val TAG = MyCasesViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _addedCases = MutableLiveData<Resource<MutableList<CaseModel>>>()
    val addedCasesList: LiveData<Resource<MutableList<CaseModel>>> = _addedCases

    private val _deletedCases = MutableLiveData<Resource<MutableList<CaseModel>>>()
    val deletedCases: LiveData<Resource<MutableList<CaseModel>>> = _deletedCases

    fun refresh() {
        setAddedCasesValue(Resource.empty())
        getMyCases()
    }

    fun getMyCases() {
        setAddedCasesValue(Resource.loading())

        val query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
                .whereEqualTo(
                    FirestoreReferences.UserIdField.value(),
                    currentLoggedInUserModel!!.id
                )
                .whereEqualTo(FirestoreReferences.IsDeletedField.value(), false)

        query.addSnapshotListener { querySnapShot, error ->
            try {
                handleAddedList(querySnapShot)
                handleDeletedList(querySnapShot)
            } catch (e: Exception) {
                Log.d(TAG, "caseList: exception $e")
                setAddedCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun handleAddedList(querySnapShot: QuerySnapshot?) {
        if (querySnapShot == null) return
        val casesList = ArrayList<CaseModel>()
        val addedList =
            querySnapShot.documentChanges.filter {
                it.type == DocumentChange.Type.ADDED ||
                        it.type == DocumentChange.Type.MODIFIED
            }
        addedList.forEach { document ->
            casesList.add(document.document.toObject(CaseModel::class.java))
        }
        setAddedCasesValue(Resource.success(casesList))
        setDeletedCasesValue(Resource.success(casesList.filter { it.isDeleted }.toMutableList()))
    }

    private fun handleDeletedList(querySnapShot: QuerySnapshot?) {
        if (querySnapShot == null) return
        val casesList = ArrayList<CaseModel>()
        val deletedList =
            querySnapShot.documentChanges.filter {
                it.type == DocumentChange.Type.REMOVED
            }
        deletedList.forEach { document ->
            casesList.add(document.document.toObject(CaseModel::class.java))
        }
        setDeletedCasesValue(Resource.success(casesList))
    }

    private fun setAddedCasesValue(success: Resource<MutableList<CaseModel>>) {
        _dataRefreshLoading.value = false
        _addedCases.value = success
    }

    private fun setDeletedCasesValue(success: Resource<MutableList<CaseModel>>) {
        _deletedCases.value = success
    }

}
