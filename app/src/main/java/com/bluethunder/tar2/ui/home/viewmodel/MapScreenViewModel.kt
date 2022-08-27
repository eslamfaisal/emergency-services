package com.bluethunder.tar2.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch


class MapScreenViewModel : ViewModel() {

    companion object {
        private val TAG = MapScreenViewModel::class.java.simpleName
    }

    private val _myCases = MutableLiveData<Resource<List<DocumentChange>>>()
    val casesList: LiveData<Resource<List<DocumentChange>>> = _myCases


    fun listenToCases() {
        setCasesValue(Resource.loading())
        val query: Query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
                .whereEqualTo(FirestoreReferences.CaseDeletedField.value(), false)

        query.addSnapshotListener { querySnapShot, error ->
            try {
                setCasesValue(Resource.success(querySnapShot!!.documentChanges))
            } catch (e: Exception) {
                setCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun setCasesValue(success: Resource<List<DocumentChange>>) {
        _myCases.value = success
    }
}
