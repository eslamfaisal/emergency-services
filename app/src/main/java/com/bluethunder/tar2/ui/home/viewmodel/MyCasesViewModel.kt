package com.bluethunder.tar2.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.FirebaseFirestore


class MyCasesViewModel : ViewModel() {

    companion object {
        private val TAG = MyCasesViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _myCases = MutableLiveData<Resource<ArrayList<CaseModel>>>()
    val myCases: LiveData<Resource<ArrayList<CaseModel>>> = _myCases

    fun refresh() {
        setCasesValue(Resource.empty())
        getMyCases()
    }

    fun getMyCases() {
        setCasesValue(Resource.loading())
        FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
            .whereEqualTo(FirestoreReferences.UserIdField.value(), currentLoggedInUserModel!!.id)
            .get().addOnSuccessListener { querySnapShot ->
                try {
                    val myCasesList = ArrayList<CaseModel>()
                    querySnapShot.documents.forEach { document ->
                        myCasesList.add(document.toObject(CaseModel::class.java)!!)
                    }
                    Log.d(TAG, "getMyCases: size  ${myCasesList.size}")
                    setCasesValue(Resource.success(myCasesList))
                } catch (e: Exception) {
                    Log.d(TAG, "getMyCases: exception $e")
                    setCasesValue(Resource.error(e.message!!))
                }
            }.addOnFailureListener {

                Log.d(TAG, "getMyCases: exception ${it.message!!}")
                setCasesValue(Resource.error(it.message!!))
            }
    }

    private fun setCasesValue(success: Resource<java.util.ArrayList<CaseModel>>) {
        _dataRefreshLoading.value = false
        _myCases.value = success
    }

}
