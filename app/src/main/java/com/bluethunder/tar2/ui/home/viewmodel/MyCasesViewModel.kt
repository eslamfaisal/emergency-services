package com.bluethunder.tar2.ui.home.viewmodel

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
                    setCasesValue(Resource.success(myCasesList))
                } catch (e: Exception) {
                    setCasesValue(Resource.error(e.message!!))
                }
            }.addOnFailureListener {
                setCasesValue(Resource.error(it.message!!))
            }
    }

    private fun setCasesValue(success: Resource<java.util.ArrayList<CaseModel>>) {
        _myCases.value = success
    }

}
